package nebulosa.desktop.gui.control

import javafx.beans.DefaultProperty
import javafx.beans.property.StringProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.layout.VBox

@DefaultProperty("child")
class LabeledPane(text: String = "") : VBox() {

    private val label = Label()

    init {
        label.text = text
        label.styleClass.add("text-xs")
        children.add(label)

        alignment = Pos.TOP_LEFT
        spacing = 1.0
        isFillWidth = true
    }

    val textProperty: StringProperty
        get() = label.textProperty()

    var text: String
        get() = label.text
        set(value) {
            label.text = value
        }

    val textStyleClass: ObservableList<String>
        get() = label.styleClass

    var child
        get() = children.firstOrNull()
        set(value) {
            label.disableProperty().unbind()

            if (children.size > 1) {
                children.removeAt(1)
            }

            if (value != null) {
                val master = value.findMaster()
                label.disableProperty().bind((master ?: value).disableProperty())
                children.add(value)
            }
        }

    var graphic: Node?
        get() = label.graphic
        set(value) {
            label.graphic = value
        }

    companion object {

        @JvmStatic
        private fun Node.findMaster(): Node? {
            if (master(this)) {
                return this
            } else if (this is Parent) {
                for (child in childrenUnmodifiable) {
                    return child.findMaster() ?: continue
                }
            }

            return null
        }

        @JvmStatic
        @JvmName("setMaster")
        fun master(node: Node, value: Boolean) {
            node.properties["labeledpane-master"] = value
            node.parent?.requestLayout()
        }

        @JvmStatic
        @JvmName("getMaster")
        fun master(node: Node): Boolean {
            return node.hasProperties() && node.properties["labeledpane-master"] == true
        }
    }
}
