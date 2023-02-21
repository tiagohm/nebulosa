package nebulosa.desktop

import javafx.application.Application
import javafx.stage.Stage
import nebulosa.desktop.gui.home.HomeWindow

class Nebulosa : Application() {

    private val homeWindow by lazy { HomeWindow() }

    override fun start(primaryStage: Stage?) {
        homeWindow.show()
    }
}
