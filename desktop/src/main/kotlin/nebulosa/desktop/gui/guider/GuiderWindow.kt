package nebulosa.desktop.gui.guider

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.util.StringConverter
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.between
import nebulosa.desktop.logic.guider.GuiderManager
import nebulosa.desktop.logic.or
import nebulosa.desktop.view.guider.GuiderType
import nebulosa.desktop.view.guider.GuiderView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class GuiderWindow : AbstractWindow("Guider", "nebulosa-guider"), GuiderView {

    @Lazy @Autowired private lateinit var guiderManager: GuiderManager

    @FXML private lateinit var guiderTypeChoiceBox: ChoiceBox<GuiderType>
    @FXML private lateinit var connectButton: Button

    init {
        title = "Guider"
        resizable = false
    }

    override fun onCreate() {
        val isConnecting = guiderManager.connectingProperty
        val isConnected = guiderManager.connectedProperty

        guiderTypeChoiceBox.converter = GuiderTypeStringConverter
        guiderTypeChoiceBox.value = GuiderType.PHD2
        guiderTypeChoiceBox.disableProperty().bind(isConnecting or isConnected)

        connectButton.disableProperty().bind(isConnecting)
        connectButton.textProperty().bind(guiderManager.connectedProperty.between("󰅙", "󱘖"))
        guiderManager.connectedProperty.between(connectButton.styleClass, "text-red-700", "text-blue-grey-700")
    }

    override val type
        get() = guiderTypeChoiceBox.value!!

    @FXML
    private fun connect() {
        guiderManager.connect(guiderTypeChoiceBox.value)
    }

    @FXML
    private fun openPHD2() {
        guiderManager.openPHD2()
    }

    private object GuiderTypeStringConverter : StringConverter<GuiderType>() {

        override fun toString(guider: GuiderType?) = guider?.label ?: "No guider selected"

        override fun fromString(text: String?) = null
    }
}
