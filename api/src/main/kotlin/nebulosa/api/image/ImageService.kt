package nebulosa.api.image

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import nebulosa.api.data.enums.HipsSurveyType
import nebulosa.api.data.enums.PlateSolverType
import nebulosa.api.data.responses.CalibrationResponse
import nebulosa.api.data.responses.FITSHeaderItemResponse
import nebulosa.api.data.responses.ImageAnnotationResponse
import nebulosa.api.data.responses.ImageInfoResponse
import nebulosa.api.framing.FramingService
import nebulosa.api.repositories.DeepSkyObjectRepository
import nebulosa.api.repositories.StarRepository
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.fits.FitsKeywords
import nebulosa.fits.dec
import nebulosa.fits.ra
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.*
import nebulosa.indi.device.mount.Mount
import nebulosa.io.transferAndClose
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.AngleFormatter
import nebulosa.math.Distance
import nebulosa.nova.position.ICRF
import nebulosa.platesolving.Calibration
import nebulosa.platesolving.astap.AstapPlateSolver
import nebulosa.platesolving.astrometrynet.LocalAstrometryNetPlateSolver
import nebulosa.platesolving.astrometrynet.NovaAstrometryNetPlateSolver
import nebulosa.platesolving.watney.WatneyPlateSolver
import nebulosa.sbd.SmallBodyDatabaseService
import nebulosa.wcs.WCSTransform
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Service
class ImageService(
    private val objectMapper: ObjectMapper,
    private val starRepository: StarRepository,
    private val deepSkyObjectRepository: DeepSkyObjectRepository,
    private val framingService: FramingService,
    private val smallBodyDatabaseService: SmallBodyDatabaseService,
) {

    private val cachedImages = HashMap<ImageToken, Image>()
    private val calibrations = HashMap<ImageToken, Calibration>()

    @Synchronized
    fun openImage(
        token: ImageToken, debayer: Boolean,
        autoStretch: Boolean = false, shadow: Float = 0f, highlight: Float = 1f, midtone: Float = 0.5f,
        mirrorHorizontal: Boolean = false, mirrorVertical: Boolean = false, invert: Boolean = false,
        scnrEnabled: Boolean = false, scnrChannel: ImageChannel = ImageChannel.GREEN, scnrAmount: Float = 0.5f,
        scnrProtectionMode: ProtectionMethod = ProtectionMethod.AVERAGE_NEUTRAL,
        output: HttpServletResponse,
    ) {
        val image = cachedImages[token]
            ?: if (token is ImageToken.Saved) Image.open(token.path, debayer).also { load(token, it) }
            else throw ResponseStatusException(HttpStatus.NOT_FOUND)

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

        val info = ImageInfoResponse(
            token.path,
            transformedImage.width,
            transformedImage.height,
            transformedImage.mono,
            stretchParams.shadow,
            stretchParams.highlight,
            stretchParams.midtone,
            transformedImage.header.ra.format(AngleFormatter.HMS),
            transformedImage.header.dec.format(AngleFormatter.SIGNED_DMS),
            token in calibrations,
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
    fun closeImage(token: ImageToken) {
        cachedImages.remove(token)
        calibrations.remove(token)
        LOG.info("image closed. token={}", token)
        System.gc()
    }

    @Synchronized
    fun annotations(
        token: ImageToken,
        stars: Boolean, dsos: Boolean, minorPlanets: Boolean,
        minorPlanetMagLimit: Double = 12.0,
    ): List<ImageAnnotationResponse> {
        val calibration = calibrations[token]

        if (calibration == null || !calibration.hasWCS || calibration.radius.value <= 0.0) {
            return emptyList()
        }

        val wcs = WCSTransform(calibration)
        val annotations = Vector<ImageAnnotationResponse>()
        val tasks = ArrayList<CompletableFuture<*>>()

        if (minorPlanets) {
            CompletableFuture.runAsync {
                val image = cachedImages[token] ?: return@runAsync
                val dateTime = image.header.getStringValue(FitsKeywords.DATE_OBS)?.ifBlank { null } ?: return@runAsync
                val latitude = image.header.getStringValue(FitsKeywords.SITELAT).deg.takeIf(Angle::valid) ?: return@runAsync
                val longitude = image.header.getStringValue(FitsKeywords.SITELONG).deg.takeIf(Angle::valid) ?: return@runAsync

                val data = smallBodyDatabaseService.identify(
                    LocalDateTime.parse(dateTime), latitude, longitude, Distance.ZERO,
                    calibration.rightAscension, calibration.declination, calibration.radius,
                    minorPlanetMagLimit,
                ).execute().body() ?: return@runAsync

                val radiusInSeconds = calibration.radius.arcsec
                var count = 0

                data.data.forEach {
                    val distance = it[5].toDouble()

                    if (distance <= radiusInSeconds) {
                        val rightAscension = it[1].hours.takeIf(Angle::valid) ?: return@forEach
                        val declination = it[2].deg.takeIf(Angle::valid) ?: return@forEach
                        val (x, y) = wcs.worldToPixel(rightAscension, declination)
                        val minorPlanet = ImageAnnotationResponse.MinorPlanet(it[0], it[1], it[2], it[6])
                        val annotation = ImageAnnotationResponse(x, y, minorPlanet = minorPlanet)
                        annotations.add(annotation)
                        count++
                    }
                }

                LOG.info("Found {} minor planets", count)
            }.also(tasks::add)
        }

        if (stars) {
            CompletableFuture.runAsync {
                starRepository
                    .search(rightAscension = calibration.rightAscension, declination = calibration.declination, radius = calibration.radius)
                    .also { LOG.info("Found {} stars", it.size) }
                    .forEach {
                        val (x, y) = wcs.worldToPixel(it.rightAscension.rad, it.declination.rad)
                        val annotation = ImageAnnotationResponse(x, y, star = it)
                        annotations.add(annotation)
                    }
            }.also(tasks::add)
        }

        if (dsos) {
            CompletableFuture.runAsync {
                deepSkyObjectRepository
                    .search(rightAscension = calibration.rightAscension, declination = calibration.declination, radius = calibration.radius)
                    .also { LOG.info("Found {} DSOs", it.size) }
                    .forEach {
                        val (x, y) = wcs.worldToPixel(it.rightAscension.rad, it.declination.rad)
                        val annotation = ImageAnnotationResponse(x, y, dso = it)
                        annotations.add(annotation)
                    }
            }.also(tasks::add)
        }

        CompletableFuture.allOf(*tasks.toTypedArray()).join()

        return annotations
    }

    fun solveImage(
        token: ImageToken, type: PlateSolverType,
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

        // TODO: Implement new solver using Image.
        require(token is ImageToken.Saved)

        val calibration = solver.solve(
            token.path, blind,
            centerRA, centerDEC, radius,
            downsampleFactor, Duration.ofMinutes(2L),
        )

        calibrations[token] = calibration

        return CalibrationResponse(calibration)
    }

    fun saveImageAs(inputPath: Path, outputPath: Path) {
        if (inputPath != outputPath) {
            if (inputPath.extension == outputPath.extension) {
                inputPath.inputStream().transferAndClose(outputPath.outputStream())
            } else {
                val image = cachedImages
                    .keys
                    .firstOrNull { it is ImageToken.Saved && it.path == inputPath }
                    ?.let { cachedImages[it] }
                    ?: return

                when (outputPath.extension.uppercase()) {
                    "PNG" -> outputPath.outputStream().use { ImageIO.write(image, "PNG", it) }
                    "JPG", "JPEG" -> outputPath.outputStream().use { ImageIO.write(image, "JPEG", it) }
                    else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid format")
                }
            }
        }
    }

    fun pointMountHere(mount: Mount, token: ImageToken, x: Double, y: Double, synchronized: Boolean) {
        val calibration = calibrations[token] ?: return
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
        val (image, calibration) = framingService
            .frame(rightAscension, declination, width, height, fov, rotation, hipsSurveyType)!!

        val token = ImageToken.Framing
        cachedImages[token] = image
        calibrations[token] = calibration

        return Path.of("@framing")
    }

    internal fun load(token: ImageToken, image: Image) {
        cachedImages[token] = image
        calibrations.remove(token)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<ImageService>()
    }
}
