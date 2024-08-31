import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import nebulosa.util.concurrency.latch.CountUpDownLatch
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class CountUpDownLatchTest {

    @Test
    fun countUpTo1AndCountDownTo0() {
        val latch = CountUpDownLatch()

        latch.countUp() shouldBeExactly 1

        measureTimeMillis {
            thread { Thread.sleep(2000L); latch.countDown() shouldBeExactly 0 }
            latch.await()
        } shouldBeGreaterThanOrEqual 2000L
    }

    @Test
    fun countUpTo2AndTimeoutOnCountDownTo1() {
        val latch = CountUpDownLatch()
        latch.countUp(2) shouldBeExactly 2
        thread { Thread.sleep(2000L); latch.countDown() shouldBeExactly 1 }
        latch.await(3L, TimeUnit.SECONDS).shouldBeFalse()
    }

    @Test
    fun countUpTo2AndCountDownTo0() {
        val latch = CountUpDownLatch()

        latch.countUp(2) shouldBeExactly 2

        measureTimeMillis {
            thread { Thread.sleep(2000L); latch.countDown() shouldBeExactly 1 }
            thread { Thread.sleep(4000L); latch.countDown() shouldBeExactly 0 }
            latch.await()
        } shouldBeGreaterThanOrEqual 4000L
    }

    @Test
    fun countUpTo10AndResetTo0() {
        val latch = CountUpDownLatch()

        latch.countUp(10) shouldBeExactly 10

        measureTimeMillis {
            thread { Thread.sleep(2000L); latch.reset() shouldBeExactly 0 }
            latch.await()
        } shouldBeGreaterThanOrEqual 2000L
    }

    @Test
    fun countUpTo5AndCountDownTo3() {
        val latch = CountUpDownLatch()

        latch.countUp(5) shouldBeExactly 5

        measureTimeMillis {
            thread { Thread.sleep(2000L); latch.countDown(2) shouldBeExactly 3 }
            latch.await(3)
        } shouldBeGreaterThanOrEqual 2000L
    }

    @Test
    fun countUpTo5AndResetTo3() {
        val latch = CountUpDownLatch()

        latch.countUp(5) shouldBeExactly 5

        measureTimeMillis {
            thread { Thread.sleep(2000L); latch.reset(3) shouldBeExactly 3 }
            latch.await(3)
        } shouldBeGreaterThanOrEqual 2000L
    }

    @Test
    fun countUpTo1AndCountDownTo0Repeatly() {
        val latch = CountUpDownLatch()

        measureTimeMillis {
            repeat(3) {
                latch.countUp() shouldBeExactly 1

                measureTimeMillis {
                    thread { Thread.sleep(2000L); latch.countDown() shouldBeExactly 0 }
                    latch.await(3L, TimeUnit.SECONDS).shouldBeTrue()
                } shouldBeGreaterThanOrEqual 2000L
            }
        } shouldBeGreaterThanOrEqual 6000L
    }

    @Test
    fun countUpToNAndCountDownTo0Repeatly() {
        val latch = CountUpDownLatch()

        measureTimeMillis {
            for (n in 1..10) {
                latch.countUp(n) shouldBeExactly n

                measureTimeMillis {
                    thread { Thread.sleep(2000L); latch.countDown(n) shouldBeExactly 0 }
                    latch.await(3L, TimeUnit.SECONDS).shouldBeTrue()
                } shouldBeGreaterThanOrEqual 2000L
            }
        } shouldBeGreaterThanOrEqual 20000L
    }
}
