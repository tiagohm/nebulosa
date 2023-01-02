package nebulosa.desktop.core.controls

import io.reactivex.rxjava3.functions.Consumer
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import nebulosa.desktop.core.eventbus.EventBus
import nebulosa.io.resource
import nebulosa.io.resourceUrl
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicBoolean

abstract class Screen(
    name: String,
    iconName: String = "nebulosa",
) : Stage(), Consumer<Any>, KoinComponent {

    private val created = AtomicBoolean(false)

    protected val eventBus by inject<EventBus>()

    init {
        setOnShowing {
            val loader = FXMLLoader(resourceUrl("$name.fxml")!!)
            loader.setController(this)
            val root = loader.load<Parent>()

            scene = Scene(root)

            icons.add(Image(resource("icons/$iconName.png")))

            if (created.compareAndSet(false, true)) onCreate()
        }

        setOnShown { onStart() }
        setOnCloseRequest { onStop() }
    }

    protected open fun onCreate() = Unit

    protected open fun onStart() = Unit

    protected open fun onStop() = Unit

    protected open fun onEvent(event: Any) = Unit

    final override fun accept(event: Any) = onEvent(event)
}
