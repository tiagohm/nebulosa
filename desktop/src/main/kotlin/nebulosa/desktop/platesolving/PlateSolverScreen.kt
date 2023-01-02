package nebulosa.desktop.platesolving

import io.reactivex.rxjava3.disposables.Disposable
import nebulosa.desktop.core.controls.Screen

class PlateSolverScreen : Screen("PlateSolver", "nebulosa-plate-solver") {

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Plate Solver"
        isResizable = false
    }

    override fun onCreate() {
        preferences.double("plateSolverManager.screen.x")?.let { x = it }
        preferences.double("plateSolverManager.screen.y")?.let { y = it }

        xProperty().addListener { _, _, value -> preferences.double("plateSolverManager.screen.x", value.toDouble()) }
        yProperty().addListener { _, _, value -> preferences.double("plateSolverManager.screen.y", value.toDouble()) }
    }

    override fun onStart() {
        subscriber = eventBus.subscribe(this)
    }

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null
    }
}
