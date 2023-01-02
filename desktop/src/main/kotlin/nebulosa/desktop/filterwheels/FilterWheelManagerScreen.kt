package nebulosa.desktop.filterwheels

import io.reactivex.rxjava3.disposables.Disposable
import javafx.fxml.FXML
import nebulosa.desktop.core.controls.Screen

class FilterWheelManagerScreen : Screen("FilterWheelManager", "nebulosa-fw-manager") {

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Filter Wheel"
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
