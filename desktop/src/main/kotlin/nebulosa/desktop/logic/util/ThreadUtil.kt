package nebulosa.desktop.logic.util

import javafx.application.Platform

inline fun javaFxThread(crossinline block: () -> Unit) {
    if (Platform.isFxApplicationThread()) block()
    else Platform.runLater { block() }
}
