package nebulosa.desktop.logic.platesolver

import java.io.File

sealed interface PlateSolvingEvent {

    val file: File
}
