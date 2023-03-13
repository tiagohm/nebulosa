package nebulosa.jmetro

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ListChangeListener
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import javafx.stage.Stage
import nebulosa.jmetro.FlatDialog.Companion.initDialog
import nebulosa.jmetro.FlatDialog.Companion.initDialogIcon

class FlatAlert(
    alertType: AlertType,
    contentText: String = "",
    vararg buttons: ButtonType,
) : Alert(alertType, contentText, *buttons) {

    val iconlessProperty: BooleanProperty = SimpleBooleanProperty(true)

    private var isResettingIcon = false

    init {
        initDialog(this, iconless)
        setupDialogIconsListener()
    }

    var iconless
        get() = iconlessProperty.get()
        set(value) {
            iconlessProperty.set(value)
        }

    private fun setupDialogIconsListener() {
        val stage = dialogPane.scene.window as Stage
        stage.icons.addListener(::dialogIconsChanged)
    }

    private fun dialogIconsChanged(change: ListChangeListener.Change<out Image>) {
        // When initOwner is called on the Dialog the
        // icon is changed and this event is fired.
        if (isResettingIcon) {
            return
        }

        // When there is an initOwner call the Dialog icon gets reset,
        // we need to reapply the JMetro icon.
        isResettingIcon = true
        initDialogIcon(this, iconless)
        isResettingIcon = false
    }
}
