package nebulosa.desktop.gui.platesolver

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Spinner
import javafx.scene.control.TextField
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.or
import nebulosa.desktop.logic.platesolver.PlateSolverManager
import nebulosa.desktop.view.platesolver.PlateSolverType
import nebulosa.desktop.view.platesolver.PlateSolverView
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.AngleFormatter
import nebulosa.platesolving.Calibration
import org.controlsfx.control.ToggleSwitch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.io.File
import java.util.concurrent.CompletableFuture
import kotlin.math.ceil

@Component
class PlateSolverWindow : AbstractWindow("PlateSolver", "nebulosa-plate-solver"), PlateSolverView {

    @Lazy @Autowired private lateinit var plateSolverManager: PlateSolverManager

    @FXML private lateinit var filePathTextField: TextField
    @FXML private lateinit var browseButton: Button
    @FXML private lateinit var plateSolverTypeChoiceBox: ChoiceBox<PlateSolverType>
    @FXML private lateinit var pathOrUrlTextField: TextField
    @FXML private lateinit var apiKeyTextField: TextField
    @FXML private lateinit var blindToggleSwitch: ToggleSwitch
    @FXML private lateinit var downsampleFactorSpinner: Spinner<Double>
    @FXML private lateinit var radiusSpinner: Spinner<Double>
    @FXML private lateinit var centerRATextField: TextField
    @FXML private lateinit var centerDECTextField: TextField
    @FXML private lateinit var raTextField: TextField
    @FXML private lateinit var decTextField: TextField
    @FXML private lateinit var orientationTextField: TextField
    @FXML private lateinit var scaleTextField: TextField
    @FXML private lateinit var fieldSizeTextField: TextField
    @FXML private lateinit var fieldRadiusTextField: TextField
    @FXML private lateinit var solveButton: Button
    @FXML private lateinit var cancelButton: Button
    @FXML private lateinit var syncButton: Button
    @FXML private lateinit var goToButton: Button
    @FXML private lateinit var slewToButton: Button

    init {
        title = "Plate Solver"
        resizable = false
    }

    override fun onCreate() {
        val isSolving = plateSolverManager.solving
        val canNotSolve = plateSolverManager.file.isNull or isSolving
        val canNotSlew = !plateSolverManager.mount.connectedProperty or plateSolverManager.mount.slewingProperty
        val canNotSync = canNotSlew or !plateSolverManager.mount.canSyncProperty
        val canNotGoTo = canNotSlew or !plateSolverManager.mount.canGoToProperty

        browseButton.disableProperty().bind(isSolving)

        plateSolverTypeChoiceBox.disableProperty().bind(isSolving)
        plateSolverTypeChoiceBox.selectionModel.selectedItemProperty().on {
            pathOrUrlTextField.promptText = if (it != null) plateSolverManager.pathOrUrl(it) else ""
            plateSolverManager.loadPathOrUrlFromPreferences()
        }

        pathOrUrlTextField.disableProperty().bind(isSolving)

        apiKeyTextField.disableProperty()
            .bind(isSolving or plateSolverTypeChoiceBox.selectionModel.selectedItemProperty().isNotEqualTo(PlateSolverType.ASTROMETRY_NET_ONLINE))

        blindToggleSwitch.disableProperty().bind(isSolving)

        downsampleFactorSpinner.disableProperty().bind(isSolving)

        centerRATextField.disableProperty().bind(isSolving or blindToggleSwitch.selectedProperty())

        centerDECTextField.disableProperty().bind(isSolving or blindToggleSwitch.selectedProperty())

        radiusSpinner.disableProperty().bind(isSolving or blindToggleSwitch.selectedProperty())

        raTextField.disableProperty().bind(canNotSolve)

        decTextField.disableProperty().bind(canNotSolve)

        orientationTextField.disableProperty().bind(canNotSolve)

        scaleTextField.disableProperty().bind(canNotSolve)

        fieldSizeTextField.disableProperty().bind(canNotSolve)

        fieldRadiusTextField.disableProperty().bind(canNotSolve)

        solveButton.disableProperty().bind(canNotSolve)

        cancelButton.disableProperty().bind(!isSolving)

        syncButton.disableProperty().bind(canNotSolve or canNotSync or !plateSolverManager.solved)

        goToButton.disableProperty().bind(canNotSolve or canNotGoTo or !plateSolverManager.solved)

        slewToButton.disableProperty().bind(canNotSolve or canNotGoTo or !plateSolverManager.solved)
    }

    override fun onStart() {
        plateSolverManager.loadPreferences()
    }

    override fun onStop() {
        plateSolverManager.savePreferences()
    }

    override fun onClose() = Unit

    override var plateSolverType: PlateSolverType
        get() = plateSolverTypeChoiceBox.value
        set(value) {
            plateSolverTypeChoiceBox.value = value
        }

    override var pathOrUrl
        get() = pathOrUrlTextField.text ?: ""
        set(value) {
            pathOrUrlTextField.text = value
        }

    override var apiKey
        get() = apiKeyTextField.text ?: ""
        set(value) {
            apiKeyTextField.text = value
        }

    override val blind
        get() = blindToggleSwitch.isSelected

    override val centerRA
        get() = Angle.from(centerRATextField.text, true)!!

    override val centerDEC
        get() = Angle.from(centerDECTextField.text)!!

    override var radius
        get() = radiusSpinner.value.deg
        set(value) {
            radiusSpinner.valueFactory.value = ceil(value.degrees)
        }

    override var downsampleFactor
        get() = downsampleFactorSpinner.value!!.toInt()
        set(value) {
            downsampleFactorSpinner.valueFactory.value = value.toDouble()
        }

    @FXML
    private fun browse() {
        plateSolverManager.browse()
    }

    @FXML
    private fun solve() {
        try {
            if (blind) {
                plateSolverManager.solve(blind = true)
            } else {
                plateSolverManager.solve(blind = false, centerRA = centerRA, centerDEC = centerDEC, radius = radius)
            }
        } catch (e: NullPointerException) {
            showAlert("Center coordinate or radius value is invalid")
        }
    }

    @FXML
    private fun cancel() {
        plateSolverManager.cancel()
    }

    @FXML
    private fun sync() {
        plateSolverManager.sync()
    }

    @FXML
    private fun goTo() {
        plateSolverManager.goTo()
    }

    @FXML
    private fun slewTo() {
        plateSolverManager.slewTo()
    }

    override fun solve(
        file: File,
        blind: Boolean,
        centerRA: Angle, centerDEC: Angle,
        radius: Angle,
    ): CompletableFuture<Calibration> {
        blindToggleSwitch.isSelected = blind
        centerRATextField.text = centerRA.format(AngleFormatter.HMS)
        centerDECTextField.text = centerDEC.format(AngleFormatter.SIGNED_DMS)
        this.radius = radius

        plateSolverManager.clearAstrometrySolution()
        return plateSolverManager.solve(file, blind, centerRA, centerDEC, radius)
    }

    override fun updateFilePath(file: File) {
        filePathTextField.text = file.name
    }

    override fun updateParameters(blind: Boolean, ra: Angle, dec: Angle) {
        blindToggleSwitch.isSelected = blind
        centerRATextField.text = ra.format(AngleFormatter.HMS)
        centerDECTextField.text = dec.format(AngleFormatter.SIGNED_DMS)
    }

    override fun updateAstrometrySolution(
        ra: Angle, dec: Angle,
        orientation: Angle, radius: Angle,
        scale: Double,
        width: Double, height: Double,
    ) {
        raTextField.text = ra.format(AngleFormatter.HMS)
        decTextField.text = dec.format(AngleFormatter.SIGNED_DMS)
        orientationTextField.text = "%.6f".format(orientation.degrees)
        scaleTextField.text = "%.6f".format(scale)
        fieldRadiusTextField.text = "%.04f".format(radius.degrees)
        fieldSizeTextField.text = "%.02f x %.02f".format(width, height)
    }
}
