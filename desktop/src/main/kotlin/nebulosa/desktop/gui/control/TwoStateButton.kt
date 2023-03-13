package nebulosa.desktop.gui.control

import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import nebulosa.desktop.gui.control.IconButton.Companion.DEFAULT_SIZE
import nebulosa.desktop.logic.on

class TwoStateButton : Button() {

    val stateProperty = SimpleBooleanProperty()

    private val stateOnLabel = Label()
    private val stateOffLabel = Label()

    init {
        alignment = Pos.CENTER
        isMnemonicParsing = false
        // minHeight = DEFAULT_SIZE
        maxHeight = DEFAULT_SIZE
        minWidth = DEFAULT_SIZE

        stateOffLabel.contentDisplay = ContentDisplay.TEXT_ONLY
        stateOnLabel.contentDisplay = ContentDisplay.TEXT_ONLY

        graphic = stateOffLabel

        stateProperty.on {
            text = if (state) stateOnText else stateOffText
            graphic = if (state) stateOnLabel else stateOffLabel
        }
    }

    var state
        get() = stateProperty.get()
        set(value) {
            stateProperty.set(value)
        }

    var stateOnIcon: String
        get() = stateOnLabel.text
        set(value) {
            stateOnLabel.text = value
        }

    var stateOffIcon: String
        get() = stateOffLabel.text
        set(value) {
            stateOffLabel.text = value
        }

    var stateOnText = ""
        set(value) {
            field = value
            text = value
        }

    var stateOffText = ""
        set(value) {
            field = value
            text = value
        }

    var size = DEFAULT_SIZE
        set(value) {
            field = value
            // minHeight = value
            maxHeight = value
            minWidth = value
        }

    val stateOnStyleClass: ObservableList<String>
        get() = stateOnLabel.styleClass

    val stateOffStyleClass: ObservableList<String>
        get() = stateOffLabel.styleClass
}
