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
