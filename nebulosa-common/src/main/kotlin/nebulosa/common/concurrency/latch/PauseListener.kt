package nebulosa.common.concurrency.latch

fun interface PauseListener {

    fun onPause(paused: Boolean)
}
