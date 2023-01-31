package nebulosa.desktop.gui

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.stage.Stage
import nebulosa.desktop.gui.home.HomeWindow
import nebulosa.desktop.logic.EventBus
import nebulosa.desktop.view.View
import nebulosa.io.resource
import nebulosa.io.resourceUrl
import org.koin.core.component.KoinComponent
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractWindow : View, KoinComponent {

    protected abstract val resourceName: String

    protected open val icon = "nebulosa"

    private val window = Stage()
    private val showingAtFirstTime = AtomicBoolean()

    init {
        window.setOnShowing {
            if (showingAtFirstTime.compareAndSet(false, true)) {
                val loader = FXMLLoader(resourceUrl("$resourceName.fxml")!!)
                loader.setController(this)
                val root = loader.load<Parent>()

                window.scene = Scene(root)
                window.icons.add(Image(resource("icons/$icon.png")))

                onCreate()

                CLOSE_EVENTBUS.subscribe {
                    if (this !is HomeWindow) {
                        close()
                        onClose()
                    }
                }
            }
        }

        window.setOnShown { onStart() }

        window.setOnHiding {
            onStop()

            if (this is HomeWindow) {
                onClose()
                CLOSE_EVENTBUS.post(Unit)
            }
        }
    }

    protected open fun onCreate() {}

    protected open fun onStart() {}

    protected open fun onStop() {}

    protected open fun onClose() {}

    override var resizable
        get() = window.isResizable
        set(value) {
            window.isResizable = value
        }

    override var maximized
        get() = window.isMaximized
        set(value) {
            window.isMaximized = value
        }

    override val showing
        get() = window.isShowing

    override var title
        get() = window.title!!
        set(value) {
            window.title = value
        }

    override var x
        get() = window.x
        set(value) {
            window.x = value
        }

    override var y
        get() = window.y
        set(value) {
            window.y = value
        }

    override var width
        get() = window.width
        set(value) {
            window.width = value
        }

    override var height
        get() = window.height
        set(value) {
            window.height = value
        }

    override val sceneWidth
        get() = window.scene.width

    override val sceneHeight
        get() = window.scene.height

    override val borderSize
        get() = (width - sceneWidth) / 2.0

    override val titleHeight
        get() = (height - sceneHeight) - borderSize

    override fun show(
        requestFocus: Boolean,
        bringToFront: Boolean,
    ) {
        window.show()

        if (requestFocus) window.requestFocus()
        if (bringToFront) window.toFront()
    }

    override fun showAndWait() {
        window.showAndWait()
    }

    override fun close() {
        window.close()
    }

    override fun showAlert(
        message: String, title: String,
    ) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = title
        alert.headerText = null
        alert.contentText = message
        alert.showAndWait()
    }

    companion object {

        @JvmStatic private val CLOSE_EVENTBUS = EventBus<Unit>()
    }
}
