package nebulosa.batch.processing

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

data class StepResult(@JvmField internal val completable: CompletableFuture<RepeatStatus>) : Future<RepeatStatus> by completable {

    constructor() : this(CompletableFuture<RepeatStatus>())

    fun complete(status: RepeatStatus): Boolean {
        return completable.complete(status)
    }

    fun completeExceptionally(e: Throwable): Boolean {
        return completable.completeExceptionally(e)
    }

    companion object {

        @JvmStatic val CONTINUABLE = StepResult(CompletableFuture.completedFuture(RepeatStatus.CONTINUABLE))
        @JvmStatic val FINISHED = StepResult(CompletableFuture.completedFuture(RepeatStatus.FINISHED))
    }
}
