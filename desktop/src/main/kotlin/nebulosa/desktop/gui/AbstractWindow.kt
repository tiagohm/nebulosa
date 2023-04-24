package nebulosa.desktop.gui

import io.reactivex.rxjava3.subjects.PublishSubject
import javafx.application.HostServices
import javafx.application.Platform
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.DialogPane
import javafx.scene.image.Image
import javafx.stage.Modality
import javafx.stage.Stage
import kotlinx.coroutines.*
import nebulosa.desktop.gui.home.HomeWindow
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.view.View
import nebulosa.io.resource
import nebulosa.io.resourceUrl
import nebulosa.jmetro.FlatAlert
import nebulosa.jmetro.JMetro
import nebulosa.jmetro.JMetroStyleClass
import nebulosa.jmetro.Style
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractWindow(
    private val resourceName: String,
    private val icon: String = "nebulosa",
    private val window: Stage = Stage(),
) : View {

    private val showingAtFirstTime = AtomicBoolean(true)
    private val mainScope = MainScope()

    @Autowired protected lateinit var beanFactory: AutowireCapableBeanFactory
        private set

    protected val hostServices by lazy { beanFactory.getBean(HostServices::class.java) }
    protected val preferences by lazy { beanFactory.getBean("preferences") as Preferences }

    init {
        window.setOnShowing {
            if (showingAtFirstTime.compareAndSet(true, false)) {
                val loader = FXMLLoader(resourceUrl("$resourceName.fxml")!!)
                loader.setController(this)
                val root = loader.load<Parent>()

                val scene = Scene(root)
                window.scene = scene
                window.icons.add(Image(resource("icons/$icon.png")))

                JMetro(window.scene, Style.DARK)
                root.styleClass.add(JMetroStyleClass.BACKGROUND)
                root.stylesheets.add("css/Global.css")

                onCreate()

                CLOSE
                    .filter { !it }
                    .subscribe {
                        if (this !is HomeWindow && initialized) {
                            use { onClose() }
                        }

                        CLOSE.onNext(true)
                    }
            }
        }

        window.setOnShown { onStart() }

        window.setOnHiding {
            onStop()

            if (this@AbstractWindow is HomeWindow) {
                mainScope.cancel()

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

    override val initialized
        get() = !showingAtFirstTime.get()

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

    final override fun show(
        requestFocus: Boolean,
        bringToFront: Boolean,
    ) {
        window.show()

        if (requestFocus) window.requestFocus()
        if (bringToFront) window.toFront()
    }

    final override fun showAndWait(owner: View?, closed: () -> Unit) {
        launch {
            if (window.owner == null) {
                window.initModality(Modality.WINDOW_MODAL)
                if (owner is AbstractWindow && owner !== this) window.initOwner(owner.window)
            }

            window.showAndWait()

            closed()
        }
    }

    final override fun close() {
        if (Platform.isFxApplicationThread()) {
            window.close()
        }
    }

    final override fun showAlert(message: String, title: String) {
        launch {
            val alert = FlatAlert(Alert.AlertType.INFORMATION)
            alert.initModality(Modality.WINDOW_MODAL)
            alert.initOwner(window)
            alert.title = title
            alert.headerText = null
            alert.contentText = message
            alert.show()
        }
    }

    final override fun showAlert(title: String, block: DialogPane.() -> Unit) {
        launch {
            val alert = FlatAlert(Alert.AlertType.INFORMATION)
            alert.initModality(Modality.WINDOW_MODAL)
            alert.initOwner(window)
            alert.title = title
            alert.dialogPane.block()
            alert.show()
        }
    }

    override fun <T : Event> addEventFilter(eventType: EventType<T>, eventFilter: EventHandler<T>) {
        window.addEventFilter(eventType, eventFilter)
    }

    override fun <T : Event> addEventHandler(eventType: EventType<T>, eventFilter: EventHandler<T>) {
        window.addEventHandler(eventType, eventFilter)
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return mainScope.launch(Dispatchers.Main, block = block)
    }

    companion object {

        @JvmStatic internal val CLOSE = PublishSubject.create<Boolean>()
    }
}
