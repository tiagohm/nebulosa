package nebulosa.common.concurrency.cancel

fun interface CancellationListener {

    fun onCancelled(source: CancellationSource)
}
