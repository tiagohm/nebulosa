package nebulosa.desktop

import javafx.application.Application
import javafx.stage.Stage
import nebulosa.desktop.gui.home.HomeWindow
import org.koin.core.component.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class Nebulosa : Application(), KoinComponent {

    private val homeWindow by lazy { HomeWindow() }

    override fun start(primaryStage: Stage) {
        loadKoinModules(module {
            single { this@Nebulosa.hostServices }
        })

        homeWindow.open()
    }
}
