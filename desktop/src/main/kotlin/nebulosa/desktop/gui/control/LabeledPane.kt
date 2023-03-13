package nebulosa.desktop.gui.control

import javafx.beans.DefaultProperty
import javafx.beans.property.StringProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.VBox

@DefaultProperty("child")
class LabeledPane(text: String = "") : VBox() {

    private val label = Label()

    init {
        label.text = text
        label.styleClass.add("text-xs")
        children.add(label)

        alignment = Pos.CENTER_LEFT
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
                label.disableProperty().bind(value.disableProperty())
                children.add(value)
            }
        }
}
