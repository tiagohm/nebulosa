package nebulosa.desktop.gui.framing

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Spinner
import javafx.scene.control.TextField
import javafx.util.StringConverter
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.framing.FramingManager
import nebulosa.desktop.logic.or
import nebulosa.desktop.view.framing.FramingView
import nebulosa.desktop.withMain
import nebulosa.hips2fits.HipsSurvey
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.AngleFormatter
import org.controlsfx.control.HyperlinkLabel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class FramingWindow : AbstractWindow("Framing", "framing"), FramingView {

    @Lazy @Autowired private lateinit var framingManager: FramingManager

    @FXML private lateinit var poweredByHyperlinkLabel: HyperlinkLabel
    @FXML private lateinit var raTextField: TextField
    @FXML private lateinit var decTextField: TextField
    @FXML private lateinit var syncButton: Button
    @FXML private lateinit var fovSpinner: Spinner<Double>
    @FXML private lateinit var widthSpinner: Spinner<Double>
    @FXML private lateinit var heightSpinner: Spinner<Double>
    @FXML private lateinit var rotationSpinner: Spinner<Double>
    @FXML private lateinit var hipsSurveyChoiceBox: ChoiceBox<HipsSurvey>
    @FXML private lateinit var loadButton: Button

    init {
        title = "Framing"
        resizable = false
    }

    override suspend fun onCreate() {
        val isLoading = framingManager.loading
        val canNotLoad = hipsSurveyChoiceBox.valueProperty().isNull

        poweredByHyperlinkLabel.setOnAction { hostServices.showDocument("https://alasky.u-strasbg.fr/hips-image-services/hips2fits") }

        raTextField.disableProperty().bind(isLoading)

        decTextField.disableProperty().bind(isLoading)

        syncButton.disableProperty().bind(isLoading or !framingManager.mount.connectedProperty)

        fovSpinner.disableProperty().bind(isLoading)

        widthSpinner.disableProperty().bind(isLoading)

        heightSpinner.disableProperty().bind(isLoading)

        rotationSpinner.disableProperty().bind(isLoading)

        hipsSurveyChoiceBox.converter = HipsSurveySourceStringConverter
        hipsSurveyChoiceBox.disableProperty().bind(isLoading)

        loadButton.disableProperty().bind(isLoading or canNotLoad)

        framingManager.populateHipsSurveys()
    }

    override suspend fun onStart() {
        framingManager.loadPreferences()
    }

    override suspend fun onStop() {
        framingManager.savePreferences()
    }

    override val hipsSurvey: HipsSurvey?
        get() = hipsSurveyChoiceBox.value

    override val frameRA
        get() = Angle.from(raTextField.text, true) ?: Angle.ZERO

    override val frameDEC
        get() = Angle.from(decTextField.text) ?: Angle.ZERO

    override val frameWidth
        get() = widthSpinner.value.toInt()

    override val frameHeight
        get() = heightSpinner.value.toInt()

    override val frameFOV
        get() = fovSpinner.value.deg

    override val frameRotation
        get() = rotationSpinner.value.deg

    @FXML
    private fun load() {
        launch { framingManager.load(frameRA, frameDEC) }
    }

    @FXML
    private fun sync() {
        launch { framingManager.sync() }
    }

    override fun populateHipsSurveys(data: List<HipsSurvey>, selected: HipsSurvey?) {
        hipsSurveyChoiceBox.items.setAll(data)
        hipsSurveyChoiceBox.value = selected
    }

    override suspend fun updateCoordinate(ra: Angle, dec: Angle) = withMain {
        raTextField.text = ra.format(AngleFormatter.HMS)
        decTextField.text = dec.format(AngleFormatter.SIGNED_DMS)
    }

    override suspend fun updateFOV(fov: Angle) = withMain {
        fovSpinner.valueFactory.value = fov.degrees
    }

    override suspend fun load(
        ra: Angle, dec: Angle,
        hips: HipsSurvey?,
        width: Int, height: Int,
        rotation: Angle, fov: Angle,
    ): Unit = withMain {
        updateCoordinate(ra, dec)

        if (hips != null) hipsSurveyChoiceBox.value = hips
        widthSpinner.valueFactory.value = width.toDouble()
        heightSpinner.valueFactory.value = height.toDouble()
        fovSpinner.valueFactory.value = fov.degrees
        rotationSpinner.valueFactory.value = rotation.degrees

        framingManager.load(ra, dec)
    }

    private object HipsSurveySourceStringConverter : StringConverter<HipsSurvey>() {

        override fun toString(source: HipsSurvey?) = if (source == null) "No image source selected"
        else "%s (%s)".format(source.id, source.regime)

        override fun fromString(text: String?) = null
    }
}
