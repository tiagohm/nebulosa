package nebulosa.desktop.platesolving

import io.reactivex.rxjava3.disposables.Disposable
import nebulosa.desktop.core.controls.Screen

class PlateSolverScreen : Screen("PlateSolver", "nebulosa-plate-solver") {

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Plate Solver"
        isResizable = false
    }

    override fun onStart() {
        subscriber = eventBus.subscribe(this)
    }

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null
    }
}
