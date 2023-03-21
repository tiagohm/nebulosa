package nebulosa.desktop.gui.control

import javafx.beans.DefaultProperty
import javafx.beans.property.ListProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import nebulosa.desktop.logic.on

@DefaultProperty("states")
class TwoStateButton : Button() {

    val stateProperty = SimpleBooleanProperty()

    val states: ListProperty<MaterialIcon> = SimpleListProperty(FXCollections.observableArrayList())

    @Volatile private var stateOff: MaterialIcon? = null
    @Volatile private var stateOn: MaterialIcon? = null

    init {
        alignment = Pos.CENTER
        isMnemonicParsing = false

        stateProperty.on {
            graphic = if (it) stateOn else stateOff
        }

        states.on {
            stateOn = states.firstOrNull { state(it) }
            stateOff = states.firstOrNull { !state(it) }
            graphic = if (state) stateOn else stateOff
        }
    }

    var state
        get() = stateProperty.get()
        set(value) {
            stateProperty.set(value)
        }

    companion object {

        @JvmStatic
        @JvmName("setState")
        fun state(node: Node, value: Boolean) {
            node.properties["twostatebutton-state"] = value
            node.parent?.requestLayout()
        }

        @JvmStatic
        @JvmName("getState")
        fun state(node: Node): Boolean {
            return node.hasProperties() && node.properties["twostatebutton-state"] == true
        }
    }
}
