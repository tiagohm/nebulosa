package nebulosa.common.concurrency.cancel

fun interface CancellationListener {

    fun onCancel(source: CancellationSource)
}
