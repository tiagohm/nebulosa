package nebulosa.desktop.core.scene

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.stage.Stage
import nebulosa.desktop.core.EventBus
import nebulosa.desktop.preferences.Preferences
import nebulosa.io.resource
import nebulosa.io.resourceUrl
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicBoolean

abstract class Screen(
    name: String,
    iconName: String = "nebulosa",
) : Stage(), KoinComponent {

    private val created = AtomicBoolean(false)

    protected val eventBus by inject<EventBus>()
    protected val preferences by inject<Preferences>()

    init {
        setOnShowing {
            if (created.compareAndSet(false, true)) {
                val loader = FXMLLoader(resourceUrl("$name.fxml")!!)
                loader.setController(this)
                val root = loader.load<Parent>()

                scene = Scene(root)

                icons.add(Image(resource("icons/$iconName.png")))

                onCreate()
            }
        }

        setOnShown { onStart() }
        setOnHiding { onStop() }
    }

    protected open fun onCreate() = Unit

    protected open fun onStart() = Unit

    protected open fun onStop() = Unit

    fun showAndFocus() {
        show()
        requestFocus()
        toFront()
    }

    companion object {

        @JvmStatic
        fun showAlert(
            message: String,
            title: String = "Information"
        ) {
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.title = title
            alert.headerText = null
            alert.contentText = message
            alert.showAndWait()
        }
    }
}
