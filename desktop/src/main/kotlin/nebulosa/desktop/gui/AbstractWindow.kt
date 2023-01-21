package nebulosa.desktop.gui

import io.reactivex.rxjava3.disposables.Disposable
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.stage.Stage
import nebulosa.desktop.core.EventBus
import nebulosa.desktop.core.EventBus.Companion.observeOnFXThread
import nebulosa.desktop.gui.home.HomeWindow
import nebulosa.io.resource
import nebulosa.io.resourceUrl
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractWindow : Stage(), KoinComponent {

    protected abstract val resourceName: String

    protected open val icon = "nebulosa"

    protected val eventBus by inject<EventBus>()

    private val showingAtFirstTime = AtomicBoolean()
    private val subscribers = arrayOfNulls<Disposable>(1)

    init {
        setOnShowing {
            if (showingAtFirstTime.compareAndSet(false, true)) {
                val loader = FXMLLoader(resourceUrl("$resourceName.fxml")!!)
                loader.setController(this)
                val root = loader.load<Parent>()

                scene = Scene(root)
                icons.add(Image(resource("icons/$icon.png")))

                onCreate()
            }
        }

        setOnShown {
            onStart()

            if (this !is HomeWindow) {
                subscribers[0] = eventBus
                    .filterIsInstance<ProgramClosed>()
                    .observeOnFXThread()
                    .subscribe {
                        onStop()

                        subscribers.forEach { it?.dispose() }
                        subscribers.fill(null)
                    }
            }
        }

        setOnHiding { onStop() }
    }

    protected open fun onCreate() {}

    protected open fun onStart() {}

    protected open fun onStop() {}

    fun open(
        requestFocus: Boolean = false,
        bringToFront: Boolean = false,
    ) {
        show()

        if (requestFocus) requestFocus()
        if (bringToFront) toFront()
    }

    companion object {

        @JvmStatic
        fun showAlert(
            message: String, title: String = "Information",
        ) {
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.title = title
            alert.headerText = null
            alert.contentText = message
            alert.showAndWait()
        }
    }
}
