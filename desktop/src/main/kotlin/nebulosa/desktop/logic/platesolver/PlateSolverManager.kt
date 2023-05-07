package nebulosa.desktop.logic.platesolver

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.stage.FileChooser
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.desktop.helper.withIO
import nebulosa.desktop.helper.withMain
import nebulosa.desktop.logic.AbstractManager
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.framing.FramingView
import nebulosa.desktop.view.platesolver.PlateSolverType
import nebulosa.desktop.view.platesolver.PlateSolverView
import nebulosa.fits.dec
import nebulosa.fits.imageHDU
import nebulosa.fits.ra
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.platesolving.Calibration
import nebulosa.platesolving.astap.AstapPlateSolver
import nebulosa.platesolving.astrometrynet.LocalAstrometryNetPlateSolver
import nebulosa.platesolving.astrometrynet.NovaAstrometryNetPlateSolver
import nebulosa.platesolving.watney.WatneyPlateSolver
import nom.tam.fits.Fits
import org.greenrobot.eventbus.EventBus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import oshi.PlatformEnum
import oshi.SystemInfo
import java.io.File
import java.time.Duration
import kotlin.math.max

@Component
class PlateSolverManager(@Autowired internal val view: PlateSolverView) : AbstractManager() {

    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var framingView: FramingView
    @Autowired private lateinit var eventBus: EventBus

    val file = SimpleObjectProperty<File>()
    val solving = SimpleBooleanProperty()
    val solved = SimpleBooleanProperty()
    val calibration = SimpleObjectProperty(Calibration.EMPTY)

    val mount
        get() = equipmentManager.selectedMount

    private fun fileWasLoaded(file: File) {
        this.file.set(file)
        view.fileWasLoaded(file)
    }

    fun clearAstrometrySolution() {
        calibration.set(Calibration.EMPTY)
    }

    fun browse() {
        with(FileChooser()) {
            title = "Open Image"

            val imageOpenPath = preferenceService.string("plateSolver.browsePath")
            if (!imageOpenPath.isNullOrBlank()) initialDirectory = File(imageOpenPath)

            extensionFilters.add(FileChooser.ExtensionFilter("All Image Files", "*.fits", "*.fit", "*.png", "*.jpeg", "*.jpg", "*.bmp"))
            extensionFilters.add(FileChooser.ExtensionFilter("FITS Files", "*.fits", "*.fit"))
            extensionFilters.add(FileChooser.ExtensionFilter("Extended Image Files", "*.png", "*.jpeg", "*.jpg", "*.bmp"))

            val file = showOpenDialog(null) ?: return

            preferenceService.string("plateSolver.browsePath", file.parent)

            view.updateParameters(true, Angle.ZERO, Angle.ZERO)

            if (file.extension.lowercase().startsWith("fit")) {
                val fits = Fits(file)

                try {
                    val header = fits.imageHDU(0)?.header

                    if (header != null) {
                        val ra = header.ra
                        val dec = header.dec

                        if (ra != null && dec != null) {
                            LOG.info("retrieved RA/DEC from fits. ra={}, dec={}", ra.hours, dec.degrees)
                            view.updateParameters(false, ra, dec)
                        }
                    }
                } catch (e: Throwable) {
                    LOG.error("failed to read RA/DEC from fits.", e)
                } finally {
                    fits.close()
                }
            }

            fileWasLoaded(file)

            clearAstrometrySolution()
        }
    }

    fun pathOrUrl(type: PlateSolverType) = when (type) {
        PlateSolverType.ASTROMETRY_NET_LOCAL -> "solve-field"
        PlateSolverType.ASTROMETRY_NET_ONLINE -> NovaAstrometryNetService.URL
        PlateSolverType.ASTAP -> if (SystemInfo.getCurrentPlatform() == PlatformEnum.WINDOWS) "C:\\Program Files\\astap\\astap.exe" else "astap"
        PlateSolverType.WATNEY -> ""
    }

    suspend fun solve(
        file: File = this.file.get(),
        blind: Boolean = view.blind,
        centerRA: Angle = view.centerRA, centerDEC: Angle = view.centerDEC,
        radius: Angle = view.radius,
    ): Calibration? {
        require(!solving.get()) { "plate solving in progress" }

        withMain { solving.set(true) }

        fileWasLoaded(file)

        return withIO {
            val pathOrUrl = when (view.type) {
                PlateSolverType.ASTROMETRY_NET_LOCAL -> view.pathOrUrl
                PlateSolverType.ASTROMETRY_NET_ONLINE -> view.pathOrUrl
                else -> view.pathOrUrl
            }.ifBlank { pathOrUrl(view.type) }

            val apiKey = view.apiKey.ifBlank { NovaAstrometryNetPlateSolver.ANONYMOUS_API_KEY }

            val solver = when (view.type) {
                PlateSolverType.ASTROMETRY_NET_LOCAL -> LocalAstrometryNetPlateSolver(pathOrUrl)
                PlateSolverType.ASTROMETRY_NET_ONLINE -> NovaAstrometryNetPlateSolver(NovaAstrometryNetService(pathOrUrl), apiKey)
                PlateSolverType.WATNEY -> WatneyPlateSolver(pathOrUrl)
                PlateSolverType.ASTAP -> AstapPlateSolver(pathOrUrl)
            }

            try {
                val calibration = solver.solve(
                    file, blind,
                    centerRA, centerDEC, radius,
                    view.downsampleFactor, PLATE_SOLVE_TIMEOUT,
                )

                savePreferences()

                eventBus.post(PlateSolvingSolved(file, calibration))

                withMain {
                    solving.set(false)
                    solved.set(true)
                    this@PlateSolverManager.calibration.set(calibration)
                }

                calibration
            } catch (e: Throwable) {
                LOG.error("plate solver failed", e)

                eventBus.post(PlateSolvingFailed(file))

                withMain {
                    solving.set(false)
                    solved.set(false)

                    if (e !is InterruptedException) {
                        view.showAlert(e.message!!)
                    }
                }

                null
            }
        }
    }

    fun sync() {
        val calibration = calibration.get() ?: return
        mount.value?.syncJ2000(calibration.rightAscension, calibration.declination)
    }

    fun goTo() {
        val calibration = calibration.get() ?: return
        mount.value?.goToJ2000(calibration.rightAscension, calibration.declination)
    }

    fun slewTo() {
        val calibration = calibration.get() ?: return
        mount.value?.slewToJ2000(calibration.rightAscension, calibration.declination)
    }

    suspend fun frame() {
        val calibration = calibration.get() ?: return
        // https://www.hnsky.org/astap.htm#viewer_angle
        val rotation = if (calibration.cdelt1.value * calibration.cdelt2.value > 0.0) (-calibration.orientation).normalized
        else calibration.orientation
        val factor = calibration.width / calibration.height
        val width = if (factor > 1.0) 1200 else 900 * factor
        val height = if (factor > 1.0) 1200 / factor else 900
        val fov = max(calibration.width.value, calibration.height.value).rad

        framingView.show(bringToFront = true)
        framingView.load(
            calibration.rightAscension, calibration.declination,
            width = width.toInt(), height = height.toInt(), rotation = rotation,
            fov = fov,
        )
    }

    fun loadPathOrUrlFromPreferences() {
        view.pathOrUrl = preferenceService.string("plateSolver.${view.type}.pathOrUrl") ?: ""
    }

    fun loadPreferences() {
        view.type = preferenceService.enum<PlateSolverType>("plateSolver.type") ?: PlateSolverType.ASTROMETRY_NET_ONLINE
        loadPathOrUrlFromPreferences()
        preferenceService.string("plateSolver.apiKey")?.let { view.apiKey = it }
        preferenceService.int("plateSolver.downsampleFactor")?.let { view.downsampleFactor = it }
        preferenceService.double("plateSolver.radius")?.let { view.radius = it.rad }
        view.updateParameters(view.blind, view.centerRA, view.centerDEC)
        preferenceService.double("plateSolver.screen.x")?.let { view.x = it }
        preferenceService.double("plateSolver.screen.y")?.let { view.y = it }
    }

    fun savePreferences() {
        if (!view.initialized) return

        preferenceService.enum("plateSolver.type", view.type)
        preferenceService.string("plateSolver.${view.type}.pathOrUrl", view.pathOrUrl)
        preferenceService.string("plateSolver.apiKey", view.apiKey)
        preferenceService.int("plateSolver.downsampleFactor", view.downsampleFactor)
        preferenceService.double("plateSolver.radius", view.radius.value)
        preferenceService.double("plateSolver.screen.x", max(0.0, view.x))
        preferenceService.double("plateSolver.screen.y", max(0.0, view.y))
    }

    override fun close() {
        savePreferences()
    }

    companion object {

        const val FOCAL_LENGTH_RATIO = 206.26480624709635

        @JvmStatic private val LOG = loggerFor<PlateSolverManager>()
        @JvmStatic private val PLATE_SOLVE_TIMEOUT = Duration.ofMinutes(5L)
    }
}
