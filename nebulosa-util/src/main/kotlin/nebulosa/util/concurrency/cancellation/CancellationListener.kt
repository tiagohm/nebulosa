package nebulosa.util.concurrency.cancellation

fun interface CancellationListener {

    fun onCancel(source: CancellationSource)
}
