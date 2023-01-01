package nebulosa.desktop

import javafx.application.Application
import javafx.stage.Stage
import nebulosa.desktop.home.HomeScreen
import org.koin.core.component.KoinComponent

class Nebulosa : Application(), KoinComponent {

    private val homeScreen by lazy { HomeScreen() }

    override fun start(primaryStage: Stage) {
        homeScreen.show()
    }
}
