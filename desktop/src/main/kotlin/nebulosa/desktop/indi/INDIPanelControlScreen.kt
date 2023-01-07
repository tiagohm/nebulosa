package nebulosa.desktop.indi

import javafx.fxml.FXML
import javafx.scene.control.ChoiceBox
import nebulosa.desktop.core.scene.Screen
import nebulosa.indi.devices.Device

class INDIPanelControlScreen(val device: Device? = null) : Screen("INDIPanelControl") {

    @FXML private lateinit var devices: ChoiceBox<Device>
}
