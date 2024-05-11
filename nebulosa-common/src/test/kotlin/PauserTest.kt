import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import nebulosa.common.concurrency.latch.Pauser
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class PauserTest : StringSpec() {

    init {
        "pause and wait for unpause" {
            val pauser = Pauser()
            pauser.isPaused.shouldBeFalse()
            pauser.pause()
            pauser.isPaused.shouldBeTrue()
            thread { Thread.sleep(1000); pauser.unpause() }
            pauser.waitForPause()
            pauser.isPaused.shouldBeFalse()
        }
        "pause and not wait for unpause" {
            val pauser = Pauser()
            pauser.isPaused.shouldBeFalse()
            pauser.pause()
            pauser.isPaused.shouldBeTrue()
            thread { Thread.sleep(1000); pauser.unpause() }
            pauser.waitForPause(500, TimeUnit.MILLISECONDS).shouldBeFalse()
            pauser.isPaused.shouldBeTrue()
        }
    }
}
