package nebulosa.desktop.mounts

import io.reactivex.rxjava3.disposables.Disposable
import javafx.fxml.FXML
import nebulosa.desktop.core.controls.Screen

class MountManagerScreen : Screen("MountManager", "nebulosa-mount-manager") {

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Mount"
        isResizable = false
    }

    override fun onCreate() {
        preferences.double("mountManager.screen.x")?.let { x = it }
        preferences.double("mountManager.screen.y")?.let { y = it }

        xProperty().addListener { _, _, value -> preferences.double("mountManager.screen.x", value.toDouble()) }
        yProperty().addListener { _, _, value -> preferences.double("mountManager.screen.y", value.toDouble()) }
    }

    override fun onStart() {
        subscriber = eventBus.subscribe(this)
    }

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null
    }

    @FXML
    private fun connect() {

    }
}
