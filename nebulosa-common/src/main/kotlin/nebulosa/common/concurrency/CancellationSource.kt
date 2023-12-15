package nebulosa.common.concurrency

sealed interface CancellationSource {

    data object None : CancellationSource

    data object Listen : CancellationSource

    data class Cancel(val mayInterruptIfRunning: Boolean) : CancellationSource

    data object Close : CancellationSource
}
