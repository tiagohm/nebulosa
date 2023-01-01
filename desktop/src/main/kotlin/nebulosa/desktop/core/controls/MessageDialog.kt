package nebulosa.desktop.core.controls

import javafx.scene.control.Alert

class MessageDialog(message: String, title: String = "Information") : Alert(AlertType.INFORMATION) {

    init {
        this.title = title
        this.headerText = null
        this.contentText = message
    }
}
