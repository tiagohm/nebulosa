package nebulosa.desktop.logic

import java.io.Closeable

interface Manager : Closeable {

    val isClosed: Boolean

    fun reset()
}
