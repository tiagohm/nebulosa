package nebulosa.desktop.logic.concurrency

import javafx.application.Platform
import java.util.concurrent.Executor

class JavaFXExecutor : Executor {

    override fun execute(command: Runnable) {
        if (Platform.isFxApplicationThread()) command.run()
        else Platform.runLater(command)
    }
}
