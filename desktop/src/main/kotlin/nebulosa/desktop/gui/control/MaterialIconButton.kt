package nebulosa.desktop.gui.control

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent

class MaterialIconButton : Button() {

    private val materialIcon = MaterialIcon()

    init {
        alignment = Pos.CENTER
        isMnemonicParsing = false
        graphic = materialIcon

        addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (contextMenu != null && it.button == MouseButton.PRIMARY) {
                contextMenu.show(it.source as Node, it.screenX, it.screenY)
                it.consume()
            }
        }
    }

    var icon
        get() = materialIcon.icon
        set(value) {
            materialIcon.icon = value
        }

    var size
        get() = materialIcon.size
        set(value) {
            materialIcon.size = value
        }

    var color
        get() = materialIcon.color
        set(value) {
            materialIcon.color = value
        }
}
