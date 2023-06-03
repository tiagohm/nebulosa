package nebulosa.desktop

import javafx.application.Application
import javafx.stage.Stage
import nebulosa.desktop.gui.home.HomeWindow
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import java.awt.EventQueue

class Nebulosa : Application() {

    override fun start(primaryStage: Stage) {
        val splash = SplashScreen()

        EventQueue.invokeLater(splash::open)

        val context = runApplication<App>(*parameters.raw.toTypedArray()) {
            addInitializers(ApplicationContextInitializer<ConfigurableApplicationContext> {
                it.beanFactory.registerSingleton("hostServices", hostServices)
                it.beanFactory.registerSingleton("primaryStage", primaryStage)
            })
        }

        EventQueue.invokeLater(splash::close)

        val homeWindow = context.getBean(HomeWindow::class.java)

        homeWindow.show()
    }
}
