package nebulosa.util.concurrency.cancellation

interface CancellationSource {

    data object None : CancellationSource

    data object Listen : CancellationSource

    data class Cancel(val mayInterruptIfRunning: Boolean) : CancellationSource

    data object Close : CancellationSource

    data class Exceptionally(val exception: Throwable) : CancellationSource

    companion object {

        @JvmStatic val DEFAULT = Cancel(true)
    }
}
