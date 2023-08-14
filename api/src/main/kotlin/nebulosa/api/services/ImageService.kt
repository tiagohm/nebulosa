package nebulosa.api.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.data.enums.HipsSurveyType
import nebulosa.api.data.enums.PlateSolverType
import nebulosa.api.data.responses.CalibrationResponse
import nebulosa.api.data.responses.FITSHeaderItemResponse
import nebulosa.api.data.responses.ImageAnnotationResponse
import nebulosa.api.data.responses.ImageInfoResponse
import nebulosa.api.repositories.DeepSkyObjectRepository
import nebulosa.api.repositories.SavedCameraImageRepository
import nebulosa.api.repositories.StarRepository
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.fits.dec
import nebulosa.fits.ra
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.*
import nebulosa.indi.device.mount.Mount
import nebulosa.io.transferAndClose
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.AngleFormatter
import nebulosa.nova.position.ICRF
import nebulosa.platesolving.Calibration
import nebulosa.platesolving.astap.AstapPlateSolver
import nebulosa.platesolving.astrometrynet.LocalAstrometryNetPlateSolver
import nebulosa.platesolving.astrometrynet.NovaAstrometryNetPlateSolver
import nebulosa.platesolving.watney.WatneyPlateSolver
import nebulosa.wcs.WCSTransform
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Path
import java.time.Duration
import javax.imageio.ImageIO
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Service
class ImageService(
    private val savedCameraImageRepository: SavedCameraImageRepository,
    private val objectMapper: ObjectMapper,
    private val starRepository: StarRepository,
    private val deepSkyObjectRepository: DeepSkyObjectRepository,
    private val framingService: FramingService,
) {

    private val cachedImages = HashMap<Path, Image>()
    private val calibrations = HashMap<Path, Calibration>()

    @Synchronized
    fun openImage(
        path: Path, debayer: Boolean,
        autoStretch: Boolean = false, shadow: Float = 0f, highlight: Float = 1f, midtone: Float = 0.5f,
        mirrorHorizontal: Boolean = false, mirrorVertical: Boolean = false, invert: Boolean = false,
        scnrEnabled: Boolean = false, scnrChannel: ImageChannel = ImageChannel.GREEN, scnrAmount: Float = 0.5f,
        scnrProtectionMode: ProtectionMethod = ProtectionMethod.AVERAGE_NEUTRAL,
        output: HttpServletResponse,
    ) {
        val image = cachedImages[path] ?: Image.open(path.toFile(), debayer)
            .also { cachedImages[path] = it }

        val manualStretch = shadow != 0f || highlight != 1f || midtone != 0.5f
        var stretchParams = ScreenTransformFunction.Parameters(midtone, shadow, highlight)

        val shouldBeTransformed = autoStretch || manualStretch
                || mirrorHorizontal || mirrorVertical || invert
                || scnrEnabled

        var transformedImage = if (shouldBeTransformed) image.clone() else image

        if (mirrorHorizontal) transformedImage = HorizontalFlip.transform(transformedImage)
        if (mirrorVertical) transformedImage = VerticalFlip.transform(transformedImage)

        if (scnrEnabled) {
            transformedImage = SubtractiveChromaticNoiseReduction(scnrChannel, scnrAmount, scnrProtectionMode).transform(transformedImage)
        }

        if (autoStretch) {
            stretchParams = AutoScreenTransformFunction.compute(transformedImage)
            transformedImage = ScreenTransformFunction(stretchParams).transform(transformedImage)
        } else if (manualStretch) {
            transformedImage = ScreenTransformFunction(stretchParams).transform(transformedImage)
        }

        if (invert) transformedImage = Invert.transform(transformedImage)

        val savedImage = savedCameraImageRepository.withPath("$path")

        val info = ImageInfoResponse(
            savedImage?.camera ?: "",
            savedImage?.path ?: "",
            savedImage?.savedAt ?: 0L,
            transformedImage.width,
            transformedImage.height,
            transformedImage.mono,
            stretchParams.shadow,
            stretchParams.highlight,
            stretchParams.midtone,
            transformedImage.header.ra?.format(AngleFormatter.HMS),
            transformedImage.header.dec?.format(AngleFormatter.SIGNED_DMS),
            path in calibrations,
            transformedImage.header.iterator().asSequence()
                .filter { it.key.isNotBlank() && !it.value.isNullOrBlank() }
                .map { FITSHeaderItemResponse(it.key, it.value ?: "") }
                .toList(),
        )

        output.addHeader("X-Image-Info", objectMapper.writeValueAsString(info))
        output.contentType = "image/png"

        ImageIO.write(transformedImage, "PNG", output.outputStream)
    }

    @Synchronized
    fun closeImage(path: Path) {
        cachedImages.remove(path)
        calibrations.remove(path)
        LOG.info("image closed. path={}", path)
        System.gc()
    }

    fun imagesOfCamera(name: String): List<SavedCameraImageEntity> {
        return savedCameraImageRepository.withCamera(name)
    }

    fun latestImageOfCamera(name: String): SavedCameraImageEntity {
        return savedCameraImageRepository.withCameraLatest(name)!!
    }

    fun savedImageOfPath(path: Path): SavedCameraImageEntity {
        return savedCameraImageRepository.withPath("$path")!!
    }

    fun annotations(
        path: Path,
        stars: Boolean, dsos: Boolean, minorPlanets: Boolean,
    ): List<ImageAnnotationResponse> {
        val calibration = calibrations[path]

        if (calibration == null || !calibration.hasWCS || calibration.radius.value <= 0.0) {
            return emptyList()
        }

        val wcs = WCSTransform(calibration)
        val annotations = arrayListOf<ImageAnnotationResponse>()

        if (stars) {
            starRepository
                .search(rightAscension = calibration.rightAscension, declination = calibration.declination, radius = calibration.radius)
                .forEach {
                    val (x, y) = wcs.worldToPixel(it.rightAscension.rad, it.declination.rad)
                    val annotation = ImageAnnotationResponse(x, y, star = it)
                    annotations.add(annotation)
                }
        }

        if (dsos) {
            deepSkyObjectRepository
                .search(rightAscension = calibration.rightAscension, declination = calibration.declination, radius = calibration.radius)
                .forEach {
                    val (x, y) = wcs.worldToPixel(it.rightAscension.rad, it.declination.rad)
                    val annotation = ImageAnnotationResponse(x, y, dso = it)
                    annotations.add(annotation)
                }
        }

        return annotations
    }

    fun solveImage(
        path: Path, type: PlateSolverType,
        blind: Boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int,
        pathOrUrl: String, apiKey: String,
    ): CalibrationResponse {
        val solver = when (type) {
            PlateSolverType.ASTROMETRY_NET_LOCAL -> LocalAstrometryNetPlateSolver(pathOrUrl)
            PlateSolverType.ASTROMETRY_NET_ONLINE -> NovaAstrometryNetPlateSolver(NovaAstrometryNetService(pathOrUrl), apiKey)
            PlateSolverType.WATNEY -> WatneyPlateSolver(pathOrUrl)
            PlateSolverType.ASTAP -> AstapPlateSolver(pathOrUrl)
        }

        val calibration = solver.solve(
            path, blind,
            centerRA, centerDEC, radius,
            downsampleFactor, Duration.ofMinutes(2L),
        )

        calibrations[path] = calibration

        return CalibrationResponse(calibration)
    }

    fun saveImageAs(inputPath: Path, outputPath: Path) {
        if (inputPath != outputPath) {
            if (inputPath.extension == outputPath.extension) {
                inputPath.inputStream().transferAndClose(outputPath.outputStream())
            } else {
                val image = cachedImages[inputPath]!!

                when (outputPath.extension.uppercase()) {
                    "PNG" -> outputPath.outputStream().use { ImageIO.write(image, "PNG", it) }
                    "JPG", "JPEG" -> outputPath.outputStream().use { ImageIO.write(image, "JPEG", it) }
                    else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid format")
                }
            }
        }
    }

    fun pointMountHere(mount: Mount, path: Path, x: Double, y: Double, synchronized: Boolean) {
        val calibration = calibrations[path] ?: return
        val wcs = WCSTransform(calibration)
        val (rightAscension, declination) = wcs.pixelToWorld(x, y)

        if (synchronized) {
            mount.goToJ2000(rightAscension, declination)
        } else {
            val icrf = ICRF.equatorial(calibration.rightAscension, calibration.declination)
            val (calibratedRA, calibratedDEC) = icrf.equatorialAtDate()
            val raOffset = calibratedRA - mount.rightAscension
            val decOffset = calibratedDEC - mount.declination
            LOG.info("pointing mount adjusted. ra={}, dec={}", raOffset.arcmin, decOffset.arcmin)
            mount.goTo(rightAscension + raOffset, declination + decOffset)
        }
    }

    fun frame(
        rightAscension: Angle, declination: Angle,
        width: Int, height: Int, fov: Angle,
        rotation: Angle = Angle.ZERO, hipsSurveyType: HipsSurveyType = HipsSurveyType.CDS_P_DSS2_COLOR,
    ): Path {
        val (image, path, calibration) = framingService
            .frame(rightAscension, declination, width, height, fov, rotation, hipsSurveyType)!!

        cachedImages[path] = image
        calibrations[path] = calibration

        return path
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<ImageService>()
    }
}
