import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldNotBeExactly
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import nebulosa.common.exec.CommandLineListener
import nebulosa.common.exec.commandLine
import nebulosa.test.NonGitHubOnly
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.time.Duration
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

@NonGitHubOnly
class CommandLineTest {

    @Test
    fun sleep() {
        val cmd = commandLine {
            executable("sleep")
            putArg("2")
        }

        measureTimeMillis {
            cmd.start().get() shouldBeExactly 0
        } shouldBeGreaterThanOrEqual 2000
    }

    @Test
    fun sleepWithTimeout() {
        val cmd = commandLine {
            executable("sleep")
            putArg("10")
        }

        measureTimeMillis {
            cmd.start(Duration.ofSeconds(2)).get() shouldNotBeExactly 0
        } shouldBeGreaterThanOrEqual 2000
    }

    @Test
    fun killSleep() {
        val cmd = commandLine {
            executable("sleep")
            putArg("10")
        }

        thread { Thread.sleep(2000); cmd.stop() }

        measureTimeMillis {
            cmd.start().get() shouldNotBeExactly 0
        } shouldBeGreaterThanOrEqual 2000 shouldBeLessThan 10000
    }

    @Test
    fun ls() {
        val lineReadListener = object : CommandLineListener.OnLineRead, ArrayList<String>(64) {

            override fun onLineRead(line: String) {
                add(line)
            }
        }

        val cmd = commandLine {
            executable("ls")
            workingDirectory(Path.of("../"))
            registerCommandLineListener(lineReadListener)
        }

        cmd.start().get() shouldBeExactly 0
        lineReadListener.shouldNotBeEmpty()
        lineReadListener.shouldContain("nebulosa-image")
    }
}
