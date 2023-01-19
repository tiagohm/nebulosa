package nebulosa.desktop.logic.concurrency

import javafx.application.Platform
import java.util.concurrent.Executor

object JavaFXThreadExecutor : Executor {

    override fun execute(command: Runnable) {
        Platform.runLater(command)
    }
}
