package nebulosa.util.concurrency.cancellation

interface Cancellable {

    fun cancel(source: CancellationSource = CancellationSource.DEFAULT): Boolean
}
