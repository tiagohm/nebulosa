package nebulosa.desktop.view

import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.control.DialogPane
import java.io.Closeable

interface View : Closeable {

    var resizable: Boolean

    var maximized: Boolean

    val showing: Boolean

    val initialized: Boolean

    var title: String

    var x: Double

    var y: Double

    var width: Double

    var height: Double

    val sceneWidth: Double

    val sceneHeight: Double

    val borderSize: Double

    val titleHeight: Double

    fun show(
        requestFocus: Boolean = false,
        bringToFront: Boolean = false,
    )

    fun showAndWait(owner: View? = null, closed: () -> Unit = {})

    fun showAlert(message: String, title: String = "Information")

    fun showAlert(title: String = "Information", block: (DialogPane) -> Unit)

    fun <T : Event> addEventFilter(eventType: EventType<T>, eventFilter: EventHandler<T>)

    fun <T : Event> addEventHandler(eventType: EventType<T>, eventFilter: EventHandler<T>)
}
