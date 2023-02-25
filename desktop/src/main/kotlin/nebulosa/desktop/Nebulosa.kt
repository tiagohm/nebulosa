package nebulosa.desktop

import javafx.application.Application
import javafx.application.HostServices
import javafx.stage.Stage
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.home.HomeWindow
import nebulosa.desktop.logic.home.HomeManager
import nebulosa.desktop.view.home.HomeView
import org.springframework.boot.runApplication
import java.util.concurrent.TimeUnit

class Nebulosa : Application() {

    override fun start(primaryStage: Stage) {
        val context = runApplication<App>()

        AbstractWindow.CLOSE
            .filter { it }
            .debounce(2L, TimeUnit.SECONDS)
            .subscribe { context.close() }

        context.beanFactory.registerResolvableDependency(HostServices::class.java, hostServices)

        val homeWindow = HomeWindow(primaryStage)
        context.beanFactory.registerResolvableDependency(HomeView::class.java, homeWindow)

        val homeManager = HomeManager(homeWindow)
        context.beanFactory.registerResolvableDependency(HomeManager::class.java, homeManager)

        context.beanFactory.autowireBean(homeWindow)
        context.beanFactory.autowireBean(homeManager)

        homeWindow.show()
    }
}
