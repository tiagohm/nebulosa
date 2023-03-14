package nebulosa.desktop.gui.control

import javafx.scene.control.ToggleButton
import nebulosa.desktop.logic.asBoolean
import org.controlsfx.control.SegmentedButton

class SwitchSegmentedButton : SegmentedButton() {

    private val stateOffToggleButton = ToggleButton()
    private val stateOnToggleButton = ToggleButton()

    val stateProperty = this.toggleGroup.selectedToggleProperty()
        .asBoolean { it === stateOnToggleButton }

    init {
        buttons.add(stateOffToggleButton)
        buttons.add(stateOnToggleButton)

        stateOffToggleButton.text = "OFF"
        stateOnToggleButton.text = "ON"

        stateOffToggleButton.minWidth = 48.0
        stateOnToggleButton.minWidth = 48.0

        toggleGroup.selectToggle(stateOffToggleButton)
        stateOffToggleButton.isSelected = true
    }

    var state
        get() = stateProperty.get()
        set(value) {
            val toggle = if (value) stateOnToggleButton else stateOffToggleButton
            toggle.isSelected = true
            toggleGroup.selectToggle(toggle)
        }
}
