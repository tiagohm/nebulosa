import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nebulosa.common.concurrency.CountUpDownLatch
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

@Suppress("OPT_IN_USAGE")
class CountUpDownLatchTest : StringSpec() {

    init {
        "count up to 1 and count down to 0" {
            val latch = CountUpDownLatch()

            latch.countUp() shouldBeExactly 1

            measureTimeMillis {
                GlobalScope.launch { delay(2000L); latch.countDown() shouldBeExactly 0 }
                latch.await()
            } shouldBeGreaterThanOrEqual 2000L
        }
        "count up to 2 and timeout on count down to 1" {
            val latch = CountUpDownLatch()
            latch.countUp(2) shouldBeExactly 2
            GlobalScope.launch { delay(2000L); latch.countDown() shouldBeExactly 1 }
            latch.await(3L, TimeUnit.SECONDS).shouldBeFalse()
        }
        "count up to 2 and count down to 0" {
            val latch = CountUpDownLatch()

            latch.countUp(2) shouldBeExactly 2

            measureTimeMillis {
                GlobalScope.launch { delay(2000L); latch.countDown() shouldBeExactly 1 }
                GlobalScope.launch { delay(4000L); latch.countDown() shouldBeExactly 0 }
                latch.await()
            } shouldBeGreaterThanOrEqual 4000L
        }
        "count up to 10 and reset to 0" {
            val latch = CountUpDownLatch()

            latch.countUp(10) shouldBeExactly 10

            measureTimeMillis {
                GlobalScope.launch { delay(2000L); latch.reset() shouldBeExactly 0 }
                latch.await()
            } shouldBeGreaterThanOrEqual 2000L
        }
        "count up to 5 and count down to 3" {
            val latch = CountUpDownLatch()

            latch.countUp(5) shouldBeExactly 5

            measureTimeMillis {
                GlobalScope.launch { delay(2000L); latch.countDown(2) shouldBeExactly 3 }
                latch.await(3)
            } shouldBeGreaterThanOrEqual 2000L
        }
        "count up to 5 and reset to 3" {
            val latch = CountUpDownLatch()

            latch.countUp(5) shouldBeExactly 5

            measureTimeMillis {
                GlobalScope.launch { delay(2000L); latch.reset(3) shouldBeExactly 3 }
                latch.await(3)
            } shouldBeGreaterThanOrEqual 2000L
        }
        "count up to 1 and count down to 0 repeatly" {
            val latch = CountUpDownLatch()

            measureTimeMillis {
                repeat(3) {
                    latch.countUp() shouldBeExactly 1

                    measureTimeMillis {
                        GlobalScope.launch { delay(2000L); latch.countDown() shouldBeExactly 0 }
                        latch.await(3L, TimeUnit.SECONDS).shouldBeTrue()
                    } shouldBeGreaterThanOrEqual 2000L
                }
            } shouldBeGreaterThanOrEqual 6000L
        }
        "count up to n and count down to 0 repeatly" {
            val latch = CountUpDownLatch()

            measureTimeMillis {
                for (n in 1..10) {
                    latch.countUp(n) shouldBeExactly n

                    measureTimeMillis {
                        GlobalScope.launch { delay(2000L); latch.countDown(n) shouldBeExactly 0 }
                        latch.await(3L, TimeUnit.SECONDS).shouldBeTrue()
                    } shouldBeGreaterThanOrEqual 2000L
                }
            } shouldBeGreaterThanOrEqual 20000L
        }
    }
}
