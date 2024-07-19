import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nebulosa.common.concurrency.cancel.CancellationSource
import nebulosa.common.concurrency.cancel.CancellationToken
import org.junit.jupiter.api.Test

class CancellationTokenTest {

    @Test
    fun cancel() {
        var source: CancellationSource? = null
        val token = CancellationToken()
        token.listen { source = it }
        token.cancel(false)
        token.get() shouldBe source
        source shouldBe CancellationSource.Cancel(false)
        token.isCancelled.shouldBeTrue()
        token.isDone.shouldBeTrue()
    }

    @Test
    fun cancelMayInterruptIfRunning() {
        var source: CancellationSource? = null
        val token = CancellationToken()
        token.listen { source = it }
        token.cancel()
        token.get() shouldBe source
        source shouldBe CancellationSource.Cancel(true)
        token.isCancelled.shouldBeTrue()
        token.isDone.shouldBeTrue()
    }

    @Test
    fun close() {
        var source: CancellationSource? = null
        val token = CancellationToken()
        token.listen { source = it }
        token.close()
        token.get() shouldBe source
        source shouldBe CancellationSource.Close
        token.isCancelled.shouldBeTrue()
        token.isDone.shouldBeTrue()
    }

    @Test
    fun listenAfterCancel() {
        var source: CancellationSource? = null
        val token = CancellationToken()
        token.cancel()
        token.listen { source = it }
        token.get() shouldBe CancellationSource.Cancel(true)
        source shouldBe CancellationSource.Listen
        token.isCancelled.shouldBeTrue()
        token.isDone.shouldBeTrue()
    }

    @Test
    fun none() {
        var source: CancellationSource? = null
        val token = CancellationToken.NONE
        token.listen { source = it }
        token.cancel()
        token.get() shouldBe CancellationSource.None
        source.shouldBeNull()
        token.isCancelled.shouldBeFalse()
        token.isDone.shouldBeTrue()
    }
}
