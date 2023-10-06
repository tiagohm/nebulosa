package nebulosa.api.image

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import nebulosa.api.framing.FramingService
import nebulosa.api.framing.HipsSurveyType
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.fits.FitsKeywords
import nebulosa.fits.dec
import nebulosa.fits.ra
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.*
import nebulosa.io.transferAndClose
import nebulosa.log.loggerFor
import nebulosa.math.*
import nebulosa.platesolving.astap.AstapPlateSolver
import nebulosa.platesolving.astrometrynet.LocalAstrometryNetPlateSolver
import nebulosa.platesolving.astrometrynet.NovaAstrometryNetPlateSolver
import nebulosa.platesolving.watney.WatneyPlateSolver
import nebulosa.sbd.SmallBodyDatabaseService
import nebulosa.simbad.SimbadCatalogType
import nebulosa.simbad.SimbadService
import nebulosa.simbad.SimbadSkyCatalog
import nebulosa.skycatalog.ClassificationType
import nebulosa.skycatalog.SkyObjectType
import nebulosa.wcs.WCSException
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
    private val framingService: FramingService,
    private val smallBodyDatabaseService: SmallBodyDatabaseService,
    private val simbadService: SimbadService,
    private val imageBucket: ImageBucket,
) {

    @Synchronized
    fun openImage(
        path: Path, debayer: Boolean,
        autoStretch: Boolean = false, shadow: Float = 0f, highlight: Float = 1f, midtone: Float = 0.5f,
        mirrorHorizontal: Boolean = false, mirrorVertical: Boolean = false, invert: Boolean = false,
        scnrEnabled: Boolean = false, scnrChannel: ImageChannel = ImageChannel.GREEN, scnrAmount: Float = 0.5f,
        scnrProtectionMode: ProtectionMethod = ProtectionMethod.AVERAGE_NEUTRAL,
        output: HttpServletResponse,
    ) {
        val image = imageBucket[path]?.first ?: imageBucket.open(path, debayer)

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

        val info = ImageInfo(
            path,
            transformedImage.width,
            transformedImage.height,
            transformedImage.mono,
            stretchParams.shadow,
            stretchParams.highlight,
            stretchParams.midtone,
            transformedImage.header.ra.format(AngleFormatter.HMS),
            transformedImage.header.dec.format(AngleFormatter.SIGNED_DMS),
            imageBucket[path]?.second != null,
            transformedImage.header.iterator().asSequence()
                .filter { it.key.isNotBlank() && !it.value.isNullOrBlank() }
                .map { ImageHeaderItem(it.key, it.value ?: "") }
                .toList(),
        )

        output.addHeader("X-Image-Info", objectMapper.writeValueAsString(info))
        output.contentType = "image/png"

        ImageIO.write(transformedImage, "PNG", output.outputStream)
    }

    @Synchronized
    fun closeImage(path: Path) {
        imageBucket.remove(path)
        LOG.info("image closed. path={}", path)
        System.gc()
    }

    @Synchronized
    fun annotations(
        path: Path,
        stars: Boolean, dsos: Boolean, minorPlanets: Boolean,
        minorPlanetMagLimit: Double = 12.0,
    ): List<ImageAnnotation> {
        val (image, calibration) = imageBucket[path] ?: return emptyList()

        if (calibration == null || calibration.isEmpty || !calibration.solved) {
            return emptyList()
        }

        val wcs = try {
            WCSTransform(calibration)
        } catch (e: WCSException) {
            LOG.error("unable to generate annotations for image. path={}", path)
            return emptyList()
        }

        val annotations = Vector<ImageAnnotation>()
        val tasks = ArrayList<CompletableFuture<*>>()

        val dateTime = image.header
            .getStringValue(FitsKeywords.DATE_OBS)
            ?.ifBlank { null }
            ?.let(LocalDateTime::parse)
            ?: LocalDateTime.now()

        if (minorPlanets && dateTime != null) {
            CompletableFuture.runAsync {
                LOG.info("finding minor planet annotations. dateTime={}, calibration={}", dateTime, calibration)

                val data = smallBodyDatabaseService.identify(
                    dateTime, 0.0, 0.0, 0.0,
                    calibration.rightAscension, calibration.declination, calibration.radius,
                    minorPlanetMagLimit,
                ).execute().body() ?: return@runAsync

                val radiusInSeconds = calibration.radius.toArcsec
                var count = 0

                data.data.forEach {
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
            }.whenComplete { _, e -> e?.printStackTrace() }.also(tasks::add)
        }

        // val barycentric = VSOP87E.EARTH.at<Barycentric>(UTC(TimeYMDHMS(dateTime)))

        if (stars || dsos) {
            CompletableFuture.runAsync {
                LOG.info("finding star annotations. dateTime={}, calibration={}", dateTime, calibration)

                val catalog = SimbadSkyCatalog(simbadService)

                val types = ArrayList<SkyObjectType>(4)

                if (stars) {
                    types.add(SkyObjectType.STAR)
                }

                if (dsos) {
                    types.add(SkyObjectType.CLUSTER_OF_STARS)
                    types.add(SkyObjectType.INTERSTELLAR_MEDIUM_OBJECT)
                    types.add(SkyObjectType.GALAXY)
                    types.add(SkyObjectType.CLUSTER_OF_GALAXIES)
                    types.add(SkyObjectType.INTERACTING_GALAXIES)
                }

                catalog.search(calibration.rightAscension, calibration.declination, calibration.radius, types)

                for (entry in catalog) {
                    if (SimbadCatalogType.entries.none { it.matches(entry.name) }) {
                        continue
                    }

                    val (x, y) = wcs.skyToPix(entry.rightAscensionJ2000, entry.declinationJ2000)
                    val annotation = if (entry.type.classification == ClassificationType.STAR) ImageAnnotation(x, y, star = entry)
                    else ImageAnnotation(x, y, dso = entry)
                    annotations.add(annotation)
                }

                LOG.info("Found {} stars/DSOs", catalog.size)
            }.whenComplete { _, e -> e?.printStackTrace() }.also(tasks::add)
        }

        CompletableFuture.allOf(*tasks.toTypedArray()).join()

        wcs.close()

        return annotations
    }

    fun solveImage(
        path: Path, type: PlateSolverType,
        blind: Boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int,
        pathOrUrl: String, apiKey: String,
    ): ImageCalibrated {
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

        imageBucket.put(path, calibration)

        return ImageCalibrated(calibration)
    }

    fun saveImageAs(inputPath: Path, outputPath: Path) {
        if (inputPath != outputPath) {
            if (inputPath.extension == outputPath.extension) {
                inputPath.inputStream().transferAndClose(outputPath.outputStream())
            } else {
                val image = imageBucket[inputPath]?.first ?: return

                when (outputPath.extension.uppercase()) {
                    "PNG" -> outputPath.outputStream().use { ImageIO.write(image, "PNG", it) }
                    "JPG", "JPEG" -> outputPath.outputStream().use { ImageIO.write(image, "JPEG", it) }
                    else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid format")
                }
            }
        }
    }

    fun frame(
        rightAscension: Angle, declination: Angle,
        width: Int, height: Int, fov: Angle,
        rotation: Angle = 0.0, hipsSurveyType: HipsSurveyType = HipsSurveyType.CDS_P_DSS2_COLOR,
    ): Path {
        val (image, calibration) = framingService
            .frame(rightAscension, declination, width, height, fov, rotation, hipsSurveyType)!!

        val path = Path.of(System.getProperty("java.io.tmpdir"), "framing")
        imageBucket.put(path, image, calibration)
        return path
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<ImageService>()
    }
}
