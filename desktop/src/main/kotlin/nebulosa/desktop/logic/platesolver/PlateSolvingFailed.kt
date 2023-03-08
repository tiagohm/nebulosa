package nebulosa.desktop.logic.platesolver

import java.io.File

data class PlateSolvingFailed(override val file: File) : PlateSolvingEvent
