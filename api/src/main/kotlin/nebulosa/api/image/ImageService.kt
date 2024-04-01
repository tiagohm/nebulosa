package nebulosa.api.image

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import nebulosa.api.atlas.Location
import nebulosa.api.atlas.SimbadEntityRepository
import nebulosa.api.calibration.CalibrationFrameService
import nebulosa.api.connection.ConnectionService
import nebulosa.api.framing.FramingService
import nebulosa.fits.*
import nebulosa.image.Image
import nebulosa.image.algorithms.computation.Histogram
import nebulosa.image.algorithms.computation.Statistics
import nebulosa.image.algorithms.transformation.*
import nebulosa.image.format.ImageChannel
import nebulosa.indi.device.camera.Camera
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.math.*
import nebulosa.nova.astrometry.VSOP87E
import nebulosa.nova.position.Barycentric
import nebulosa.sbd.SmallBodyDatabaseService
import nebulosa.skycatalog.ClassificationType
import nebulosa.skycatalog.SkyObjectType
import nebulosa.star.detection.ImageStar
import nebulosa.star.detection.StarDetector
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import nebulosa.wcs.WCS
import nebulosa.wcs.WCSException
import nebulosa.xisf.XisfFormat
import okio.sink
import org.springframework.http.HttpStatus
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO
import kotlin.io.path.extension
import kotlin.io.path.outputStream

@Service
class ImageService(
    private val objectMapper: ObjectMapper,
    private val framingService: FramingService,
    private val calibrationFrameService: CalibrationFrameService,
    private val smallBodyDatabaseService: SmallBodyDatabaseService,
    private val simbadEntityRepository: SimbadEntityRepository,
    private val imageBucket: ImageBucket,
    private val threadPoolTaskExecutor: ThreadPoolTaskExecutor,
    private val connectionService: ConnectionService,
    private val starDetector: StarDetector<Image>,
) {

    val fovCameras: ByteArray by lazy {
        URI.create("https://github.com/tiagohm/nebulosa.data/raw/main/astrobin/cameras.json")
            .toURL().openConnection().getInputStream().readAllBytes()
    }

    val fovTelescopes: ByteArray by lazy {
        URI.create("https://github.com/tiagohm/nebulosa.data/raw/main/astrobin/telescopes.json")
            .toURL().openConnection().getInputStream().readAllBytes()
    }

    @Synchronized
    fun openImage(
        path: Path, camera: Camera?, debayer: Boolean = true, calibrate: Boolean = false, force: Boolean = false,
        autoStretch: Boolean = false, shadow: Float = 0f, highlight: Float = 1f, midtone: Float = 0.5f,
        mirrorHorizontal: Boolean = false, mirrorVertical: Boolean = false, invert: Boolean = false,
        scnrEnabled: Boolean = false, scnrChannel: ImageChannel = ImageChannel.GREEN, scnrAmount: Float = 0.5f,
        scnrProtectionMode: ProtectionMethod = ProtectionMethod.AVERAGE_NEUTRAL,
        output: HttpServletResponse,
    ) {
        val image = imageBucket.open(path, debayer, force = force)

        val manualStretch = shadow != 0f || highlight != 1f || midtone != 0.5f
        var stretchParams = ScreenTransformFunction.Parameters(midtone, shadow, highlight)

        val shouldBeTransformed = autoStretch || manualStretch
                || mirrorHorizontal || mirrorVertical || invert
                || scnrEnabled

        var transformedImage = if (shouldBeTransformed) image.clone() else image
        val instrument = camera?.name ?: image.header.instrument

        if (calibrate && !instrument.isNullOrBlank()) {
            transformedImage = calibrationFrameService.calibrate(instrument, transformedImage, transformedImage === image)
        }

        if (mirrorHorizontal) {
            transformedImage = HorizontalFlip.transform(transformedImage)
        }
        if (mirrorVertical) {
            transformedImage = VerticalFlip.transform(transformedImage)
        }

        if (scnrEnabled) {
            transformedImage = SubtractiveChromaticNoiseReduction(scnrChannel, scnrAmount, scnrProtectionMode).transform(transformedImage)
        }

        val statistics = transformedImage.compute(Statistics.GRAY)

        if (autoStretch) {
            stretchParams = AutoScreenTransformFunction.compute(transformedImage)
            transformedImage = ScreenTransformFunction(stretchParams).transform(transformedImage)
        } else if (manualStretch) {
            transformedImage = ScreenTransformFunction(stretchParams).transform(transformedImage)
        }

        if (invert) {
            transformedImage = Invert.transform(transformedImage)
        }

        val info = ImageInfo(
            path,
            transformedImage.width, transformedImage.height, transformedImage.mono,
            stretchParams.shadow, stretchParams.highlight, stretchParams.midtone,
            transformedImage.header.rightAscension.takeIf { it.isFinite() },
            transformedImage.header.declination.takeIf { it.isFinite() },
            imageBucket[path]?.second?.let(::ImageSolved),
            transformedImage.header.mapNotNull { if (it.isCommentStyle) null else ImageHeaderItem(it.key, it.value) },
            instrument?.let(connectionService::camera),
            statistics,
        )

        output.addHeader("X-Image-Info", objectMapper.writeValueAsString(info))
        output.contentType = "image/png"

        ImageIO.write(transformedImage, "PNG", output.outputStream)
    }

    @Synchronized
    fun closeImage(path: Path) {
        imageBucket.remove(path)
        LOG.info("image closed. path={}", path)
    }

    @Synchronized
    fun annotations(
        path: Path,
        starsAndDSOs: Boolean, minorPlanets: Boolean,
        minorPlanetMagLimit: Double = 12.0,
        location: Location? = null,
    ): List<ImageAnnotation> {
        val (image, calibration) = imageBucket[path] ?: return emptyList()

        if (calibration.isNullOrEmpty() || !calibration.solved) {
            return emptyList()
        }

        val wcs = try {
            WCS(calibration)
        } catch (e: WCSException) {
            LOG.error("unable to generate annotations for image. path={}", path)
            return emptyList()
        }

        val annotations = Vector<ImageAnnotation>(64)
        val tasks = ArrayList<CompletableFuture<*>>(2)

        val dateTime = image.header.observationDate ?: LocalDateTime.now()

        if (minorPlanets) {
            threadPoolTaskExecutor.submitCompletable {
                val latitude = image.header.latitude ?: location?.latitude?.deg ?: 0.0
                val longitude = image.header.longitude ?: location?.longitude?.deg ?: 0.0

                LOG.info(
                    "finding minor planet annotations. dateTime={}, latitude={}, longitude={}, calibration={}",
                    dateTime, latitude.formatSignedDMS(), longitude.formatSignedDMS(), calibration
                )

                val identifiedBody = smallBodyDatabaseService.identify(
                    dateTime, latitude, longitude, 0.0,
                    calibration.rightAscension, calibration.declination, calibration.radius,
                    minorPlanetMagLimit,
                ).execute().body() ?: return@submitCompletable

                val radiusInSeconds = calibration.radius.toArcsec
                var count = 0

                identifiedBody.data.forEach {
                    val distance = it[5].toDouble()

                    if (distance <= radiusInSeconds) {
                        val rightAscension = it[1].hours.takeIf(Angle::isFinite) ?: return@forEach
                        val declination = it[2].deg.takeIf(Angle::isFinite) ?: return@forEach
                        val (x, y) = wcs.skyToPix(rightAscension, declination)
                        val minorPlanet = ImageAnnotation.MinorPlanet(0L, it[0], rightAscension, declination, it[6].toDouble())
                        val annotation = ImageAnnotation(x, y, minorPlanet = minorPlanet)
                        annotations.add(annotation)
                        count++
                    }
                }

                LOG.info("Found {} minor planets", count)
            }.whenComplete { _, e -> e?.printStackTrace() }
                .also(tasks::add)
        }

        if (starsAndDSOs) {
            threadPoolTaskExecutor.submitCompletable {
                val barycentric = VSOP87E.EARTH.at<Barycentric>(UTC(TimeYMDHMS(dateTime)))

                LOG.info("finding star/DSO annotations. dateTime={}, calibration={}", dateTime, calibration)

                val catalog = simbadEntityRepository.find(null, null, calibration.rightAscension, calibration.declination, calibration.radius)

                var count = 0

                for (entry in catalog) {
                    if (entry.type == SkyObjectType.EXTRA_SOLAR_PLANET) continue

                    val astrometric = barycentric.observe(entry).equatorial()

                    LOG.debug {
                        "%s: %s %s -> %s %s".format(
                            entry.name,
                            entry.rightAscensionJ2000.formatHMS(), entry.declinationJ2000.formatSignedDMS(),
                            astrometric.longitude.normalized.formatHMS(), astrometric.latitude.formatSignedDMS(),
                        )
                    }

                    val (x, y) = wcs.skyToPix(astrometric.longitude.normalized, astrometric.latitude)
                    val annotation = if (entry.type.classification == ClassificationType.STAR) ImageAnnotation(x, y, star = entry)
                    else ImageAnnotation(x, y, dso = entry)
                    annotations.add(annotation)
                    count++
                }

                LOG.info("Found {} stars/DSOs", count)
            }.whenComplete { _, e -> e?.printStackTrace() }
                .also(tasks::add)
        }

        tasks.forEach { it.get() }

        wcs.close()

        return annotations
    }

    fun saveImageAs(inputPath: Path, outputPath: Path) {
        if (inputPath != outputPath) {
            val image = imageBucket[inputPath]?.first
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found")

            when (outputPath.extension.uppercase()) {
                "PNG" -> outputPath.outputStream().use { ImageIO.write(image, "PNG", it) }
                "JPG", "JPEG" -> outputPath.outputStream().use { ImageIO.write(image, "JPEG", it) }
                "FIT", "FITS" -> outputPath.sink().use { image.writeTo(it, FitsFormat) }
                "XISF" -> outputPath.sink().use { image.writeTo(it, XisfFormat) }
                else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid format")
            }
        }
    }

    fun frame(
        rightAscension: Angle, declination: Angle,
        width: Int, height: Int, fov: Angle,
        rotation: Angle = 0.0, id: String = "CDS/P/DSS2/COLOR",
    ): Path {
        val (image, calibration, path) = framingService
            .frame(rightAscension, declination, width, height, fov, rotation, id)!!
        imageBucket.put(path, image, calibration)
        return path
    }

    fun coordinateInterpolation(path: Path): CoordinateInterpolation? {
        val (image, calibration) = imageBucket[path] ?: return null

        if (calibration.isNullOrEmpty() || !calibration.solved) {
            return null
        }

        val wcs = try {
            WCS(calibration)
        } catch (e: WCSException) {
            LOG.error("unable to generate annotations for image. path={}", path)
            return null
        }

        val delta = COORDINATE_INTERPOLATION_DELTA
        val width = image.width + (image.width % delta).let { if (it == 0) 0 else delta - it }
        val xIter = 0..width step delta
        val height = image.height + (image.height % delta).let { if (it == 0) 0 else delta - it }
        val yIter = 0..height step delta

        val md = DoubleArray(xIter.count() * yIter.count())
        val ma = DoubleArray(md.size)
        var count = 0

        for (y in yIter) {
            for (x in xIter) {
                val (rightAscension, declination) = wcs.pixToSky(x.toDouble(), y.toDouble())
                ma[count] = rightAscension.toDegrees
                md[count] = declination.toDegrees
                count++
            }
        }

        return CoordinateInterpolation(ma, md, 0, 0, width, height, delta, image.header.observationDate)
    }

    fun detectStars(path: Path): List<ImageStar> {
        val (image) = imageBucket[path] ?: return emptyList()
        return starDetector.detect(image)
    }

    fun histogram(path: Path, bitLength: Int = 16): IntArray {
        val (image) = imageBucket[path] ?: return IntArray(0)
        return image.compute(Histogram(bitLength = bitLength))
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<ImageService>()

        private const val COORDINATE_INTERPOLATION_DELTA = 24
    }
}
