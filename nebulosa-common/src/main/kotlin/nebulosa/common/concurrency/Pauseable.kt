package nebulosa.common.concurrency

interface Pauseable {

    val paused: Boolean

    fun pause()

    fun unpause()
}
