package nebulosa.api.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.data.enums.PlateSolverType
import nebulosa.api.data.responses.CalibrationResponse
import nebulosa.api.data.responses.ImageAnnotationResponse
import nebulosa.api.repositories.DeepSkyObjectRepository
import nebulosa.api.repositories.SavedCameraImageRepository
import nebulosa.api.repositories.StarRepository
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.fits.dec
import nebulosa.fits.ra
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.*
import nebulosa.io.transferAndClose
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.AngleFormatter
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
) {

    private val cachedImages = HashMap<Path, Image>()
    private val calibrations = HashMap<Path, Calibration>()

    @Synchronized
    fun openImage(
        path: Path, debayer: Boolean,
        autoStretch: Boolean, shadow: Float, highlight: Float, midtone: Float,
        mirrorHorizontal: Boolean, mirrorVertical: Boolean, invert: Boolean,
        scnrEnabled: Boolean, scnrChannel: ImageChannel, scnrAmount: Float, scnrProtectionMode: ProtectionMethod,
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

        val info = savedCameraImageRepository.withPath("$path")

        output.addHeader(
            "X-Image-Info", objectMapper.writeValueAsString(
                mapOf(
                    "id" to (info?.id ?: 0L),
                    "name" to (info?.name ?: ""),
                    "path" to (info?.path ?: ""),
                    "savedAt" to (info?.savedAt ?: 0L),
                    "width" to transformedImage.width,
                    "height" to transformedImage.height,
                    "mono" to transformedImage.mono,
                    "stretchShadow" to stretchParams.shadow,
                    "stretchHighlight" to stretchParams.highlight,
                    "stretchMidtone" to stretchParams.midtone,
                    "rightAscension" to transformedImage.header.ra?.format(AngleFormatter.HMS),
                    "declination" to transformedImage.header.dec?.format(AngleFormatter.SIGNED_DMS),
                )
            )
        )

        output.contentType = "image/png"

        ImageIO.write(transformedImage, "PNG", output.outputStream)
    }

    @Synchronized
    fun closeImage(path: Path) {
        cachedImages.remove(path)
        calibrations.remove(path)
        System.gc()
    }

    fun imagesOfCamera(name: String): List<SavedCameraImageEntity> {
        return savedCameraImageRepository.withName(name)
    }

    fun latestImageOfCamera(name: String): SavedCameraImageEntity {
        return savedCameraImageRepository.withNameLatest(name)!!
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

    fun pointMountHere(path: Path, x: Double, y: Double) {
        val calibration = calibrations[path] ?: return
        val wcs = WCSTransform(calibration)
        val (rightAscension, declination) = wcs.pixelToWorld(x, y)
    }
}
