package nebulosa.desktop.platesolving

import io.reactivex.rxjava3.disposables.Disposable
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.scene.Screen

class PlateSolverScreen : Screen("PlateSolver", "nebulosa-plate-solver") {

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Plate Solver"
        isResizable = false
    }

    override fun onCreate() {
        preferences.double("plateSolverManager.screen.x")?.let { x = it }
        preferences.double("plateSolverManager.screen.y")?.let { y = it }

        xProperty().on { preferences.double("plateSolverManager.screen.x", it) }
        yProperty().on { preferences.double("plateSolverManager.screen.y", it) }
    }

    override fun onStart() {}

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null
    }
}
