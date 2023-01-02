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

    override fun onCreate() {
        preferences.double("filterWheelManager.screen.x")?.let { x = it }
        preferences.double("filterWheelManager.screen.y")?.let { y = it }

        xProperty().addListener { _, _, value -> preferences.double("filterWheelManager.screen.x", value.toDouble()) }
        yProperty().addListener { _, _, value -> preferences.double("filterWheelManager.screen.y", value.toDouble()) }
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
