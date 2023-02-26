package nebulosa.desktop.logic.platesolver

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.stage.FileChooser
import nebulosa.astrometrynet.nova.NovaAstrometryNetService
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
                try {
                    val header = Fits(file).imageHDU(0)?.header

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
                }
            }

            updateFile(file)

            clearAstrometrySolution()
        }
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
            val pathOrUrl = if (view.plateSolverType == PlateSolverType.ASTROMETRY_NET_LOCAL) view.pathOrUrl.ifBlank { ASTROMETRY_NET_LOCAL_PATH }
            else view.pathOrUrl.ifBlank { ASTROMETRY_NET_ONLINE_URL }

            val solver = if (view.plateSolverType == PlateSolverType.ASTROMETRY_NET_LOCAL) LocalAstrometryNetPlateSolver(pathOrUrl)
            else NovaAstrometryNetPlateSolver(NovaAstrometryNetService(pathOrUrl), view.apiKey.ifBlank { "XXXXXXXX" })

            val tempFile = File.createTempFile("platesolver", ".tmp")

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

    fun loadPathOrUrlFromPreferences() {
        view.pathOrUrl = if (view.plateSolverType == PlateSolverType.ASTROMETRY_NET_LOCAL) preferences.string("plateSolver.path") ?: ""
        else preferences.string("plateSolver.url") ?: ""
    }

    fun loadPreferences() {
        preferences.enum<PlateSolverType>("plateSolver.plateSolverType")?.let { view.plateSolverType = it }
        loadPathOrUrlFromPreferences()
        preferences.string("plateSolver.apiKey")?.let { view.apiKey = it }
        preferences.double("plateSolver.radius")?.let { view.radius = it.rad }
        preferences.double("plateSolver.screen.x")?.let { view.x = it }
        preferences.double("plateSolver.screen.y")?.let { view.y = it }
    }

    fun savePreferences() {
        preferences.enum("plateSolver.plateSolverType", view.plateSolverType)
        if (view.plateSolverType == PlateSolverType.ASTROMETRY_NET_LOCAL) preferences.string("plateSolver.path", view.pathOrUrl)
        else preferences.string("plateSolver.url", view.pathOrUrl)
        preferences.string("plateSolver.apiKey", view.apiKey)
        preferences.double("plateSolver.radius", view.radius.value)
        preferences.double("plateSolver.screen.x", view.x)
        preferences.double("plateSolver.screen.y", view.y)
    }

    companion object {

        const val ASTROMETRY_NET_LOCAL_PATH = "solve-field"
        const val ASTROMETRY_NET_ONLINE_URL = "https://nova.astrometry.net/api/"
        const val FOCAL_LENGTH_RATIO = 206.26480624709635

        @JvmStatic private val LOG = LoggerFactory.getLogger(PlateSolverManager::class.java)
        @JvmStatic private val PLATE_SOLVE_TIMEOUT = Duration.ofMinutes(5L)
    }
}
