package nebulosa.desktop.focusers

import io.reactivex.rxjava3.disposables.Disposable
import javafx.fxml.FXML
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.scene.Screen

class FocuserManagerScreen : Screen("FocuserManager", "nebulosa-focuser-manager") {

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Focuser"
        isResizable = false
    }

    override fun onCreate() {
        preferences.double("focuserManager.screen.x")?.let { x = it }
        preferences.double("focuserManager.screen.y")?.let { y = it }

        xProperty().on { preferences.double("focuserManager.screen.x", it) }
        yProperty().on { preferences.double("focuserManager.screen.y", it) }
    }

    override fun onStart() {}

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null
    }

    @FXML
    private fun connect() {

    }
}
