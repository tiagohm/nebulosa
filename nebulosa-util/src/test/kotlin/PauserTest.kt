import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import nebulosa.util.concurrency.latch.Pauser
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class PauserTest {

    @Test
    fun pauseAndWaitForUnpause() {
        val pauser = Pauser()
        pauser.isPaused.shouldBeFalse()
        pauser.pause()
        pauser.isPaused.shouldBeTrue()
        thread { Thread.sleep(1000); pauser.unpause() }
        pauser.waitForPause()
        pauser.isPaused.shouldBeFalse()
    }

    @Test
    fun pauseAndNotWaitForUnpause() {
        val pauser = Pauser()
        pauser.isPaused.shouldBeFalse()
        pauser.pause()
        pauser.isPaused.shouldBeTrue()
        thread { Thread.sleep(1000); pauser.unpause() }
        pauser.waitForPause(500, TimeUnit.MILLISECONDS).shouldBeFalse()
        pauser.isPaused.shouldBeTrue()
    }
}
