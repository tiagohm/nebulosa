package nebulosa.api.image

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.ContentType
import io.ktor.server.response.respondOutputStream
import io.ktor.server.routing.RoutingCall
import nebulosa.api.atlas.Location
import nebulosa.api.atlas.SkyObjectEntityRepository
import nebulosa.api.calibration.CalibrationFrameService
import nebulosa.api.connection.ConnectionService
import nebulosa.api.framing.FramingService
import nebulosa.api.image.ImageAnnotation.StarDSO
import nebulosa.api.ktor.responseHeaders
import nebulosa.api.message.MessageService
import nebulosa.fits.*
import nebulosa.image.Image
import nebulosa.image.algorithms.computation.Statistics
import nebulosa.image.algorithms.transformation.*
import nebulosa.image.format.ImageChannel
import nebulosa.image.format.ImageHdu
import nebulosa.image.format.ImageModifier
import nebulosa.indi.device.camera.Camera
import nebulosa.log.*
import nebulosa.math.*
import nebulosa.nova.astrometry.VSOP87E
import nebulosa.nova.position.Barycentric
import nebulosa.sbd.SmallBodyDatabaseService
import nebulosa.simbad.SimbadSearch
import nebulosa.simbad.SimbadService
import nebulosa.skycatalog.ClassificationType
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import nebulosa.time.SystemClock
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import nebulosa.wcs.WCS
import nebulosa.wcs.WCSException
import nebulosa.xisf.XisfFormat
import nebulosa.xisf.isXisf
import nebulosa.xisf.xisf
import okio.sink
import java.net.URI
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import javax.imageio.ImageIO
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.outputStream
import kotlin.math.roundToInt

class ImageService(
    private val mapper: ObjectMapper,
    private val framingService: FramingService,
    private val calibrationFrameService: CalibrationFrameService,
    private val smallBodyDatabaseService: SmallBodyDatabaseService,
    private val skyObjectEntityRepository: SkyObjectEntityRepository,
    private val simbadService: SimbadService,
    private val imageBucket: ImageBucket,
    private val executorService: ExecutorService,
    private val connectionService: ConnectionService,
    private val messageService: MessageService,
) {

    private enum class ImageOperation {
        OPEN,
        SAVE,
        STATISTICS,
    }

    private data class TransformedImage(
        @JvmField val image: Image,
        @JvmField val stretchParameters: ScreenTransformFunction.Parameters? = null,
        @JvmField val instrument: Camera? = null,
    )

    val fovCameras: ByteArray by lazy {
        URI.create("https://github.com/tiagohm/nebulosa.data/raw/main/astrobin/cameras.json")
            .toURL().openConnection().getInputStream().readAllBytes()
    }

    val fovTelescopes: ByteArray by lazy {
        URI.create("https://github.com/tiagohm/nebulosa.data/raw/main/astrobin/telescopes.json")
            .toURL().openConnection().getInputStream().readAllBytes()
    }

    suspend fun openImage(path: Path, camera: Camera?, transformation: ImageTransformation, output: RoutingCall) {
        val (image, calibration) = imageBucket.open(path, transformation.debayer, force = transformation.force)
        val (transformedImage, stretchParameters, instrument) = image!!.transform(true, transformation, ImageOperation.OPEN, camera)

        val info = ImageInfo(
            path,
            transformedImage.width, transformedImage.height, transformedImage.mono, CfaPattern.from(image.header),
            transformation.stretch.copy(
                shadow = (stretchParameters!!.shadow * 65536f).roundToInt(),
                highlight = (stretchParameters.highlight * 65536f).roundToInt(),
                midtone = (stretchParameters.midtone * 65536f).roundToInt(),
            ),
            transformedImage.header.rightAscension.takeIf { it.isFinite() },
            transformedImage.header.declination.takeIf { it.isFinite() },
            calibration?.let(::ImageSolved),
            transformedImage.header.mapNotNull { if (it.isCommentStyle) null else ImageHeaderItem(it.key, it.value) },
            transformedImage.header.bitpix, instrument,
        )

        val format = if (transformation.useJPEG) "jpeg" else "png"
        val contentType = if (transformation.useJPEG) ContentType.Image.JPEG else ContentType.Image.PNG

        output.responseHeaders.append(X_IMAGE_INFO_HEADER_KEY, mapper.writeValueAsString(info))

        output.respondOutputStream(contentType) { ImageIO.write(transformedImage, format, this) }

        LOG.d("image opened. path={}", path)
    }

    private fun Image.transform(
        enabled: Boolean, transformation: ImageTransformation,
        operation: ImageOperation, camera: Camera? = null
    ): TransformedImage {
        val instrument = camera ?: header.instrument?.let(connectionService::camera)

        val (autoStretch, shadow, highlight, midtone) = transformation.stretch
        val scnrEnabled = transformation.scnr.channel != null
        val manualStretch = shadow != 0 || highlight != 65536 || midtone != 32768

        val shouldBeTransformed = enabled && (autoStretch || manualStretch
                || transformation.mirrorHorizontal || transformation.mirrorVertical || transformation.invert
                || scnrEnabled)

        var transformedImage = if (shouldBeTransformed) clone() else this

        if (enabled && !transformation.calibrationGroup.isNullOrBlank()) {
            transformedImage = calibrationFrameService.calibrate(transformation.calibrationGroup, transformedImage, transformedImage === this)
        }

        if (enabled && transformation.mirrorHorizontal) {
            transformedImage = HorizontalFlip.transform(transformedImage)
        }
        if (enabled && transformation.mirrorVertical) {
            transformedImage = VerticalFlip.transform(transformedImage)
        }

        if (enabled && scnrEnabled) {
            val (channel, amount, method) = transformation.scnr
            transformedImage = SubtractiveChromaticNoiseReduction(channel!!, amount, method)
                .transform(transformedImage)
        }

        var stretchParams = ScreenTransformFunction.Parameters.DEFAULT

        if (enabled && operation != ImageOperation.STATISTICS) {
            if (autoStretch) {
                stretchParams = AdaptativeScreenTransformFunction(transformation.stretch.meanBackground).compute(transformedImage)
                transformedImage = ScreenTransformFunction(stretchParams).transform(transformedImage)
            } else if (manualStretch) {
                stretchParams = ScreenTransformFunction.Parameters(midtone, shadow, highlight)
                transformedImage = ScreenTransformFunction(stretchParams).transform(transformedImage)
            }
        }

        if (enabled && transformation.invert) {
            transformedImage = Invert.transform(transformedImage)
        }

        return TransformedImage(transformedImage, stretchParams, instrument)
    }

    fun openImageOnDesktop(paths: Iterable<Path>) {
        paths.forEach { messageService.sendMessage(OpenImageEvent(it)) }
    }

    @Synchronized
    fun closeImage(path: Path) {
        imageBucket.remove(path)
        LOG.d("image closed. path={}", path)
    }

    @Synchronized
    fun annotations(path: Path, request: AnnotateImageRequest, location: Location? = null): List<ImageAnnotation> {
        val (image, calibration) = imageBucket.open(path)

        if (image == null || calibration.isNullOrEmpty() || !calibration.solved) {
            return emptyList()
        }

        val wcs = try {
            WCS(calibration)
        } catch (e: WCSException) {
            LOG.e("unable to generate annotations for image. path={}", path, e)
            return emptyList()
        }

        val annotations = Vector<ImageAnnotation>(64)
        val tasks = ArrayList<CompletableFuture<*>>(2)

        val dateTime = image.header.observationDate ?: LocalDateTime.now(SystemClock)

        if (request.minorPlanets) {
            CompletableFuture.runAsync({
                val latitude = image.header.latitude ?: location?.latitude?.deg ?: 0.0
                val longitude = image.header.longitude ?: location?.longitude?.deg ?: 0.0

                LOG.di("finding minor planet annotations. dateTime={}, latitude={}, longitude={}, calibration={}", dateTime, latitude.formatSignedDMS(), longitude.formatSignedDMS(), calibration)

                val identifiedBody = smallBodyDatabaseService.identify(
                    dateTime, latitude, longitude, 0.0,
                    calibration.rightAscension, calibration.declination, calibration.radius,
                    request.minorPlanetMagLimit, !request.includeMinorPlanetsWithoutMagnitude,
                ).execute().body() ?: return@runAsync

                val radiusInSeconds = calibration.radius.toArcsec
                var count = 0

                identifiedBody.data.forEach {
                    val distance = it[5].toDouble()

                    if (distance <= radiusInSeconds) {
                        val rightAscension = it[1].hours.takeIf(Angle::isFinite) ?: return@forEach
                        val declination = it[2].deg.takeIf(Angle::isFinite) ?: return@forEach
                        val (x, y) = wcs.skyToPix(rightAscension, declination)

                        if (x >= 0 && y >= 0 && x < image.width && y < image.height) {
                            val magnitude = it[6].replace(INVALID_MAG_CHARS, "").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE
                            val minorPlanet = ImageAnnotation.MinorPlanet(0L, listOf(it[0]), rightAscension, declination, magnitude)
                            val annotation = ImageAnnotation(x, y, minorPlanet = minorPlanet)
                            annotations.add(annotation)
                            count++
                        }
                    }
                }

                LOG.i("found {} minor planets", count)
            }, executorService)
                .whenComplete { _, e -> e?.printStackTrace() }
                .also(tasks::add)
        }

        if (request.starsAndDSOs) {
            CompletableFuture.runAsync({
                LOG.di("finding star/DSO annotations. dateTime={}, useSimbad={}, calibration={}", dateTime, request.useSimbad, calibration)

                val rightAscension = calibration.rightAscension
                val declination = calibration.declination
                val radius = calibration.radius

                val catalog = if (request.useSimbad) {
                    simbadService.search(SimbadSearch.Builder().region(rightAscension, declination, radius).build())
                } else {
                    skyObjectEntityRepository.search(null, null, rightAscension, declination, radius)
                }

                var count = 0
                val barycentric = VSOP87E.EARTH.at<Barycentric>(UTC(TimeYMDHMS(dateTime)))

                for (entry in catalog) {
                    if (entry.type == SkyObjectType.EXTRA_SOLAR_PLANET) continue

                    val astrometric = barycentric.observe(entry).equatorial()

                    val (x, y) = wcs.skyToPix(astrometric.longitude.normalized, astrometric.latitude)

                    if (x >= 0 && y >= 0 && x < image.width && y < image.height) {
                        val annotation = if (entry.type.classification == ClassificationType.STAR) ImageAnnotation(x, y, star = StarDSO(entry))
                        else ImageAnnotation(x, y, dso = StarDSO(entry))
                        annotations.add(annotation)
                        count++
                    }
                }

                LOG.i("found {} stars/DSOs", count)
            }, executorService)
                .whenComplete { _, e -> e?.printStackTrace() }
                .also(tasks::add)
        }

        tasks.forEach { it.get() }

        wcs.close()

        return annotations
    }

    fun saveImageAs(path: Path, save: SaveImage) {
        require(save.path != null)

        var (image) = imageBucket.open(path).image?.transform(save.shouldBeTransformed, save.transformation, ImageOperation.SAVE)
            ?: throw IllegalArgumentException("image not found")

        val (x, y, width, height) = save.subFrame.constrained(image.width, image.height)

        if (width > 0 && height > 0 && (x > 0 || y > 0 || width != image.width || height != image.height)) {
            LOG.d("image subframed. x={}, y={}, width={}, height={}", x, y, width, height)
            image = image.transform(SubFrame(x, y, width, height))
        }

        val modifier = ImageModifier
            .bitpix(save.bitpix)

        when (save.format) {
            ImageExtension.FITS -> save.path.sink().use { image.writeTo(it, FitsFormat, modifier) }
            ImageExtension.XISF -> save.path.sink().use { image.writeTo(it, XisfFormat, modifier) }
            ImageExtension.PNG -> save.path.outputStream().use { ImageIO.write(image, "PNG", it) }
            ImageExtension.JPG -> save.path.outputStream().use { ImageIO.write(image, "JPEG", it) }
        }
    }

    fun analyze(path: Path): ImageAnalyzed? {
        if (!path.exists() || !path.isRegularFile()) return null

        val image = if (path.isFits()) path.fits()
        else if (path.isXisf()) path.xisf()
        else return null

        return image.use { it.firstOrNull { hdu -> hdu is ImageHdu }?.header }
            ?.let(::ImageAnalyzed)
    }

    fun frame(
        rightAscension: Angle, declination: Angle,
        width: Int, height: Int, fov: Angle,
        rotation: Angle = 0.0, id: String = "CDS/P/DSS2/COLOR",
    ): Path {
        val (image, calibration, path) = framingService.frame(rightAscension, declination, width, height, fov, rotation, id)!!
        imageBucket.put(path, image, calibration)
        return path
    }

    fun coordinateInterpolation(path: Path): CoordinateInterpolation? {
        val (image, calibration) = imageBucket.open(path)

        if (image == null || calibration.isNullOrEmpty() || !calibration.solved) {
            return null
        }

        val wcs = try {
            WCS(calibration)
        } catch (e: WCSException) {
            LOG.e("unable to generate annotations for image. path={}", path, e)
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

    fun statistics(path: Path, transformation: ImageTransformation, channel: ImageChannel, camera: Camera?): Statistics.Data {
        val (image) = imageBucket.open(path, transformation.debayer)
        val (transformedImage) = image!!.transform(true, transformation, ImageOperation.STATISTICS, camera)
        return transformedImage.compute(Statistics.CHANNELS[channel] ?: return Statistics.Data.EMPTY)
    }

    companion object {

        private val LOG = loggerFor<ImageService>()
        private val INVALID_MAG_CHARS = "[^.\\-+0-9]+".toRegex()

        const val X_IMAGE_INFO_HEADER_KEY = "X-Image-Info"
        const val COORDINATE_INTERPOLATION_DELTA = 24
    }
}
