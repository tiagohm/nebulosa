package nebulosa.common.concurrency.latch

interface Pauseable {

    val isPaused: Boolean

    fun pause()

    fun unpause()
}
