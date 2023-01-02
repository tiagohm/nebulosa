package nebulosa.desktop.focusers

import io.reactivex.rxjava3.disposables.Disposable
import javafx.fxml.FXML
import nebulosa.desktop.core.controls.Screen

class FocuserManagerScreen : Screen("FocuserManager", "nebulosa-focuser-manager") {

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Focuser"
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
