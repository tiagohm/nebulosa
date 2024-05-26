import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nebulosa.common.concurrency.cancel.CancellationSource
import nebulosa.common.concurrency.cancel.CancellationToken

class CancellationTokenTest : StringSpec() {

    init {
        "cancel" {
            var source: CancellationSource? = null
            val token = CancellationToken()
            token.listen { source = it }
            token.cancel(false)
            token.get() shouldBe source
            source shouldBe CancellationSource.Cancel(false)
            token.isCancelled.shouldBeTrue()
        }
        "cancel may interrupt if running" {
            var source: CancellationSource? = null
            val token = CancellationToken()
            token.listen { source = it }
            token.cancel()
            token.get() shouldBe source
            source shouldBe CancellationSource.Cancel(true)
            token.isCancelled.shouldBeTrue()
        }
        "close" {
            var source: CancellationSource? = null
            val token = CancellationToken()
            token.listen { source = it }
            token.close()
            token.get() shouldBe source
            source shouldBe CancellationSource.Close
            token.isCancelled.shouldBeTrue()
        }
        "listen" {
            var source: CancellationSource? = null
            val token = CancellationToken()
            token.cancel()
            token.listen { source = it }
            token.get() shouldBe CancellationSource.Cancel(true)
            source shouldBe CancellationSource.Listen
            token.isCancelled.shouldBeTrue()
        }
        "none" {
            var source: CancellationSource? = null
            val token = CancellationToken.NONE
            token.isCancelled.shouldBeTrue()
            token.listen { source = it }
            token.cancel()
            token.get() shouldBe CancellationSource.None
            source.shouldBeNull()
        }
    }
}
