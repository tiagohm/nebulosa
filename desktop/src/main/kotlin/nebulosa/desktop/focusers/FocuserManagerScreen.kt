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

    override fun onCreate() {
        preferences.double("focuserManager.screen.x")?.let { x = it }
        preferences.double("focuserManager.screen.y")?.let { y = it }

        xProperty().addListener { _, _, value -> preferences.double("focuserManager.screen.x", value.toDouble()) }
        yProperty().addListener { _, _, value -> preferences.double("focuserManager.screen.y", value.toDouble()) }
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
