package nebulosa.desktop

import javafx.application.Application
import javafx.stage.Stage
import nebulosa.desktop.home.Home
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Nebulosa : Application(), KoinComponent {

    private val home by inject<Home>()

    override fun start(primaryStage: Stage) {
        home.show()
    }
}
