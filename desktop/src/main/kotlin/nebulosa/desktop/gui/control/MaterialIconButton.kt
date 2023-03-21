package nebulosa.desktop.gui.control

import javafx.geometry.Pos
import javafx.scene.control.Button

class MaterialIconButton : Button() {

    private val materialIcon = MaterialIcon()

    init {
        alignment = Pos.CENTER
        isMnemonicParsing = false
        graphic = materialIcon
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
