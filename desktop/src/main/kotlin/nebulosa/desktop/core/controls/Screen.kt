package nebulosa.desktop.core.controls

import io.reactivex.rxjava3.functions.Consumer
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import nebulosa.desktop.core.eventbus.EventBus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("LeakingThis")
abstract class Screen(name: String) : Stage(), Consumer<Any>, KoinComponent {

    protected val eventBus by inject<EventBus>()

    init {
        val resource = Thread.currentThread().contextClassLoader.getResource("$name.fxml")!!
        val loader = FXMLLoader(resource)
        loader.setController(this)
        val root = loader.load<Parent>()

        scene = Scene(root)

        setOnShown { onStart() }
        setOnCloseRequest { onStop() }
    }

    protected open fun onStart() = Unit

    protected open fun onStop() = Unit

    protected open fun onEvent(event: Any) = Unit

    final override fun accept(event: Any) = onEvent(event)
}
