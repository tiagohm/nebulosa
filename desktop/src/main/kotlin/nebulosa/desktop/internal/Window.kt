package nebulosa.desktop.internal

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("LeakingThis")
abstract class Window(name: String) : Stage(), KoinComponent {

    protected val eventBus by inject<EventBus>()

    init {
        title = name
        val resource = Thread.currentThread().contextClassLoader.getResource("$name.fxml")!!
        val loader = FXMLLoader(resource)
        loader.setController(this)
        val root = loader.load<Parent>()
        scene = Scene(root)

        setOnShowing {
            eventBus.register(this)
            onStart()
        }

        setOnCloseRequest {
            eventBus.unregister(this)
            onStop()
        }
    }

    protected open fun onStart() = Unit

    protected open fun onStop() = Unit

    protected open fun onEventReceived(event: Any) = Unit

    @Synchronized
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun handleEventReceived(event: Any) {
        onEventReceived(event)
    }
}
