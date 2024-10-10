package nebulosa.util.concurrency.latch

fun interface PauseListener {

    fun onPause(paused: Boolean)
}
