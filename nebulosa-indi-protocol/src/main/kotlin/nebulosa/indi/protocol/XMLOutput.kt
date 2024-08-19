package nebulosa.indi.protocol

import java.io.PrintStream

sealed interface XMLOutput {

    fun writeTo(stream: PrintStream)
}
