package nebulosa.desktop

import javafx.application.Application
import javafx.stage.Stage
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.home.HomeWindow
import nebulosa.desktop.logic.home.HomeManager
import org.springframework.boot.runApplication
import java.awt.EventQueue
import java.util.concurrent.TimeUnit

class Nebulosa : Application() {

    override fun start(primaryStage: Stage) {
        val splash = SplashScreen()

        EventQueue.invokeLater(splash::open)

        val context = runApplication<App>(*parameters.raw.toTypedArray())

        AbstractWindow.CLOSE
            .filter { it }
            .debounce(2L, TimeUnit.SECONDS)
            .subscribe { context.close() }

        context.beanFactory.registerSingleton("hostServices", hostServices)

        val homeWindow = HomeWindow(primaryStage)
        context.beanFactory.registerSingleton("homeView", homeWindow)

        val homeManager = HomeManager(homeWindow)
        context.beanFactory.registerSingleton("homeManager", homeManager)

        context.beanFactory.autowireBean(homeWindow)
        context.beanFactory.autowireBean(homeManager)

        EventQueue.invokeLater(splash::close)

        homeWindow.show()
    }
}
