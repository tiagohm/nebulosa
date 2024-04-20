package nebulosa.common.concurrency.cancel

fun interface CancellationListener {

    fun cancelledBy(source: CancellationSource)
}
