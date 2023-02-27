package nebulosa.desktop.gui

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.stage.Stage
import nebulosa.desktop.gui.home.HomeWindow
import nebulosa.desktop.logic.newEventBus
import nebulosa.desktop.view.View
import nebulosa.io.resource
import nebulosa.io.resourceUrl
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractWindow(
    private val resourceName: String,
    private val icon: String = "nebulosa",
    private val window: Stage = Stage(),
) : View {

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

                CLOSE
                    .filter { !it }
                    .subscribe {
                        if (this !is HomeWindow) {
                            use { onClose() }
                        }

                        CLOSE.onNext(true)
                    }
            }
        }

        window.setOnShown { onStart() }

        window.setOnHiding {
            onStop()

            if (this is HomeWindow) {
                onClose()
                CLOSE.onNext(false)
            }
        }
    }

    protected open fun onCreate() = Unit

    protected open fun onStart() = Unit

    protected open fun onStop() = Unit

    protected open fun onClose() = Unit

    final override var resizable
        get() = window.isResizable
        set(value) {
            window.isResizable = value
        }

    final override var maximized
        get() = window.isMaximized
        set(value) {
            window.isMaximized = value
        }

    final override val showing
        get() = window.isShowing

    final override var title
        get() = window.title!!
        set(value) {
            window.title = value
        }

    final override var x
        get() = window.x
        set(value) {
            window.x = value
        }

    final override var y
        get() = window.y
        set(value) {
            window.y = value
        }

    final override var width
        get() = window.width
        set(value) {
            window.width = value
        }

    final override var height
        get() = window.height
        set(value) {
            window.height = value
        }

    final override val sceneWidth
        get() = window.scene.width

    final override val sceneHeight
        get() = window.scene.height

    final override val borderSize
        get() = (width - sceneWidth) / 2.0

    final override val titleHeight
        get() = (height - sceneHeight) - borderSize

    @Synchronized
    final override fun show(
        requestFocus: Boolean,
        bringToFront: Boolean,
    ) {
        window.show()

        if (requestFocus) window.requestFocus()
        if (bringToFront) window.toFront()
    }

    final override fun showAndWait() {
        window.showAndWait()
    }

    final override fun close() {
        window.close()
    }

    final override fun showAlert(
        message: String, title: String,
    ) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.initOwner(window)
        alert.title = title
        alert.headerText = null
        alert.contentText = message
        alert.showAndWait()
    }

    companion object {

        @JvmStatic internal val CLOSE = newEventBus<Boolean>()
    }
}
