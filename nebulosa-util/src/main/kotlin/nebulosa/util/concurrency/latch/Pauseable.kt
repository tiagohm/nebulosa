package nebulosa.util.concurrency.latch

interface Pauseable {

    val isPaused: Boolean

    fun pause()

    fun unpause()
}
