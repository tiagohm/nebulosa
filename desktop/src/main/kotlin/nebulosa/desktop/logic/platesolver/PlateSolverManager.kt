package nebulosa.desktop.logic.platesolver

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.stage.FileChooser
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
import nebulosa.desktop.App
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.logic.util.javaFxThread
import nebulosa.desktop.view.platesolver.PlateSolverType
import nebulosa.desktop.view.platesolver.PlateSolverView
import nebulosa.fits.dec
import nebulosa.fits.imageHDU
import nebulosa.fits.ra
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.platesolving.Calibration
import nebulosa.platesolving.astap.AstapPlateSolver
import nebulosa.platesolving.astrometrynet.LocalAstrometryNetPlateSolver
import nebulosa.platesolving.astrometrynet.NovaAstrometryNetPlateSolver
import nom.tam.fits.Fits
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicReference

@Component
class PlateSolverManager(@Autowired private val view: PlateSolverView) {

    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var systemExecutorService: ExecutorService
    @Autowired private lateinit var osType: App.OSType

    private val solverTask = AtomicReference<Future<*>>()

    val file = SimpleObjectProperty<File>()
    val solving = SimpleBooleanProperty()
    val solved = SimpleBooleanProperty()
    val calibration = SimpleObjectProperty<Calibration>()

    val mount
        get() = equipmentManager.selectedMount

    private fun updateFile(file: File) {
        this.file.set(file)
        view.updateFilePath(file)
    }

    fun clearAstrometrySolution() {
        view.updateAstrometrySolution(Angle.ZERO, Angle.ZERO, Angle.ZERO, Angle.ZERO, 0.0, 0.0, 0.0)
        calibration.set(null)
    }

    fun browse() {
        with(FileChooser()) {
            title = "Open Image"

            val imageOpenPath = preferences.string("plateSolver.browsePath")
            if (!imageOpenPath.isNullOrBlank()) initialDirectory = File(imageOpenPath)

            extensionFilters.add(FileChooser.ExtensionFilter("All Image Files", "*.fits", "*.fit", "*.png", "*.jpeg", "*.jpg", "*.bmp"))
            extensionFilters.add(FileChooser.ExtensionFilter("FITS Files", "*.fits", "*.fit"))
            extensionFilters.add(FileChooser.ExtensionFilter("Extended Image Files", "*.png", "*.jpeg", "*.jpg", "*.bmp"))

            val file = showOpenDialog(null) ?: return

            preferences.string("plateSolver.browsePath", file.parent)

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

            updateFile(file)

            clearAstrometrySolution()
        }
    }

    fun pathOrUrl(type: PlateSolverType) = when (type) {
        PlateSolverType.ASTROMETRY_NET_LOCAL -> "solve-field"
        PlateSolverType.ASTROMETRY_NET_ONLINE -> NovaAstrometryNetService.URL
        PlateSolverType.ASTAP -> if (osType == App.OSType.WINDOWS) "C:\\Program Files\\astap\\astap.exe" else "astap"
    }

    @Synchronized
    fun solve(
        file: File = this.file.get(),
        blind: Boolean = true,
        centerRA: Angle = Angle.ZERO, centerDEC: Angle = Angle.ZERO,
        radius: Angle = Angle.ZERO,
    ): CompletableFuture<Calibration> {
        require(!solving.get()) { "plate solving in progress" }

        solving.set(true)

        updateFile(file)

        val future = CompletableFuture<Calibration>()

        val task = systemExecutorService.submit {
            val pathOrUrl = when (view.plateSolverType) {
                PlateSolverType.ASTROMETRY_NET_LOCAL -> view.pathOrUrl
                PlateSolverType.ASTROMETRY_NET_ONLINE -> view.pathOrUrl
                else -> view.pathOrUrl
            }.ifBlank { pathOrUrl(view.plateSolverType) }

            val apiKey = view.apiKey.ifBlank { NovaAstrometryNetPlateSolver.ANONYMOUS_API_KEY }

            val solver = when (view.plateSolverType) {
                PlateSolverType.ASTROMETRY_NET_LOCAL -> LocalAstrometryNetPlateSolver(pathOrUrl)
                PlateSolverType.ASTROMETRY_NET_ONLINE -> NovaAstrometryNetPlateSolver(NovaAstrometryNetService(pathOrUrl), apiKey)
                else -> AstapPlateSolver(pathOrUrl)
            }

            val tempFile = File.createTempFile("platesolver", ".${file.extension}")

            LOG.info("create temp file for plate solving. path={}", tempFile)

            try {
                file.inputStream().use { input -> tempFile.outputStream().use(input::transferTo) }

                val calibration = solver.solve(
                    tempFile, blind,
                    centerRA, centerDEC, radius,
                    view.downsampleFactor, PLATE_SOLVE_TIMEOUT,
                )

                savePreferences()

                future.complete(calibration)

                javaFxThread {
                    solving.set(false)
                    solved.set(true)

                    view.updateAstrometrySolution(
                        calibration.ra, calibration.dec,
                        calibration.orientation, calibration.radius,
                        calibration.scale,
                        calibration.width, calibration.height,
                    )
                }
            } catch (e: Throwable) {
                future.completeExceptionally(e)

                javaFxThread {
                    solving.set(false)
                    solved.set(false)

                    if (e !is InterruptedException) {
                        view.showAlert(e.message!!)
                    }
                }
            } finally {
                tempFile.delete()
            }
        }

        solverTask.set(task)

        return future
    }

    fun cancel() {
        solverTask.getAndSet(null)?.cancel(true)
    }

    fun sync() {
        val calibration = calibration.get() ?: return
        mount.value?.syncJ2000(calibration.ra, calibration.dec)
    }

    fun goTo() {
        val calibration = calibration.get() ?: return
        mount.value?.goToJ2000(calibration.ra, calibration.dec)
    }

    fun slewTo() {
        val calibration = calibration.get() ?: return
        mount.value?.slewToJ2000(calibration.ra, calibration.dec)
    }

    fun loadPathOrUrlFromPreferences() {
        view.pathOrUrl = if (view.plateSolverType == PlateSolverType.ASTROMETRY_NET_ONLINE) preferences.string("plateSolver.url") ?: ""
        else preferences.string("plateSolver.path") ?: ""
    }

    fun loadPreferences() {
        view.plateSolverType = preferences.enum<PlateSolverType>("plateSolver.plateSolverType") ?: PlateSolverType.ASTROMETRY_NET_ONLINE
        loadPathOrUrlFromPreferences()
        preferences.string("plateSolver.apiKey")?.let { view.apiKey = it }
        preferences.int("plateSolver.downsampleFactor")?.let { view.downsampleFactor = it }
        preferences.double("plateSolver.radius")?.let { view.radius = it.rad }
        preferences.double("plateSolver.screen.x")?.let { view.x = it }
        preferences.double("plateSolver.screen.y")?.let { view.y = it }
    }

    fun savePreferences() {
        preferences.enum("plateSolver.plateSolverType", view.plateSolverType)
        if (view.plateSolverType == PlateSolverType.ASTROMETRY_NET_LOCAL) preferences.string("plateSolver.path", view.pathOrUrl)
        else preferences.string("plateSolver.url", view.pathOrUrl)
        preferences.string("plateSolver.apiKey", view.apiKey)
        preferences.int("plateSolver.downsampleFactor", view.downsampleFactor)
        preferences.double("plateSolver.radius", view.radius.value)
        preferences.double("plateSolver.screen.x", view.x)
        preferences.double("plateSolver.screen.y", view.y)
    }

    companion object {

        const val FOCAL_LENGTH_RATIO = 206.26480624709635

        @JvmStatic private val LOG = LoggerFactory.getLogger(PlateSolverManager::class.java)
        @JvmStatic private val PLATE_SOLVE_TIMEOUT = Duration.ofMinutes(5L)
    }
}
