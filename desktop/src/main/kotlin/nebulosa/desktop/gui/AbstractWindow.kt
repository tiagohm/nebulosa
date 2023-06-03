package nebulosa.desktop.gui

import jakarta.annotation.PostConstruct
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
import nebulosa.desktop.service.PreferenceService
import nebulosa.desktop.view.View
import nebulosa.desktop.view.WindowedView
import nebulosa.io.resource
import nebulosa.io.resourceUrl
import nebulosa.jmetro.FlatAlert
import nebulosa.jmetro.JMetro
import nebulosa.jmetro.JMetroStyleClass
import nebulosa.jmetro.Style
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ConfigurableApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

abstract class AbstractWindow(
    private val resourceName: String,
    private val icon: String = "nebulosa",
) : WindowedView {

    private val showingAtFirstTime = AtomicBoolean(true)
    private val mainScope = MainScope()

    override val window by lazy { Stage() }

    @Autowired protected lateinit var beanFactory: AutowireCapableBeanFactory
        private set

    @Autowired protected lateinit var hostServices: HostServices
        private set

    @Autowired protected lateinit var preferenceService: PreferenceService
        private set

    @Autowired private lateinit var configurableApplicationContext: ConfigurableApplicationContext

    @PostConstruct
    protected fun initialize() {
        window.setOnShowing {
            if (showingAtFirstTime.compareAndSet(true, false)) {
                val loader = FXMLLoader(resourceUrl("screens/$resourceName.fxml")!!)
                loader.setController(this)
                val root = loader.load<Parent>()

                val scene = Scene(root)
                window.scene = scene
                window.icons.add(Image(resource("icons/$icon.png")))

                JMetro(window.scene, Style.DARK)
                root.styleClass.add(JMetroStyleClass.BACKGROUND)
                root.stylesheets.add("css/Global.css")

                onCreate()

                synchronized(windowBucket) { windowBucket.add(this) }
            }
        }

        window.setOnShown { onStart() }

        window.setOnHiding {
            onStop()

            if (this is HomeWindow) {
                mainScope.cancel()

                onClose()

                with(windowBucket.filter { it !== this }) {
                    forEach(AbstractWindow::close)
                    forEach(AbstractWindow::onClose)
                }
            }

            synchronized(windowBucket) {
                windowBucket.remove(this)

                if (windowBucket.isEmpty()) {
                    configurableApplicationContext.close()
                    exitProcess(0)
                }
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

    final override fun showAndWait(owner: View?, onClose: suspend CoroutineScope.() -> Unit) {
        launch {
            if (window.owner == null) {
                window.initModality(Modality.WINDOW_MODAL)
                if (owner is WindowedView && owner !== this) window.initOwner(owner.window)
            }

            window.showAndWait()

            onClose()
        }
    }

    final override fun close() {
        if (initialized && showing && Platform.isFxApplicationThread()) {
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
            alert.dialogPane.stylesheets.add("css/Global.css")
            alert.show()
        }
    }

    final override fun showAlert(title: String, block: DialogPane.() -> Unit) {
        launch {
            val alert = FlatAlert(Alert.AlertType.INFORMATION)
            alert.initModality(Modality.WINDOW_MODAL)
            alert.initOwner(window)
            alert.title = title
            alert.dialogPane.stylesheets.add("css/Global.css")
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

        @JvmStatic private val windowBucket = HashSet<AbstractWindow>()
    }
}
