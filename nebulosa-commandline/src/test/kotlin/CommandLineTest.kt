import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import nebulosa.commandline.CommandLine
import nebulosa.commandline.CommandLineHandler
import nebulosa.commandline.CommandLineListener
import nebulosa.test.NonGitHubOnly
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.system.measureTimeMillis

@NonGitHubOnly
class CommandLineTest {

    @Test
    fun execute() {
        val lines = ArrayList<String>()
        val handler = CommandLineHandler()
        handler.registerCommandLineListener(object : CommandLineListener {

            override fun onLineRead(line: String) {
                lines.add(line)
            }
        })

        val cmd = CommandLine(listOf("ls"))
        cmd.execute(handler).exitCode shouldBeExactly 0
        lines shouldHaveSize 3 shouldContainAll listOf("build", "build.gradle.kts", "src")
    }

    @Test
    fun executeWithWorkingDirectory() {
        val lines = ArrayList<String>()
        val handler = CommandLineHandler()
        handler.registerCommandLineListener(object : CommandLineListener {

            override fun onLineRead(line: String) {
                lines.add(line)
            }
        })

        val cmd = CommandLine(listOf("ls"), workingDirectory = Path("../"))
        cmd.execute(handler).exitCode shouldBeExactly 0
        lines shouldHaveSize 81 shouldContainAll listOf("README.md")
    }

    @Test
    fun executeWithEnvironment() {
        val lines = ArrayList<String>()
        val handler = CommandLineHandler()
        handler.registerCommandLineListener(object : CommandLineListener {

            override fun onLineRead(line: String) {
                lines.add(line)
            }
        })

        val cmd = CommandLine(listOf("/bin/bash", "-c", "echo \$TEST"), environment = mapOf("TEST" to "123456"))
        cmd.execute(handler).exitCode shouldBeExactly 0
        lines shouldHaveSize 1 shouldContainAll listOf("123456")
    }

    @Test
    fun executeWithTimeout() {
        var e: Throwable? = null
        val handler = CommandLineHandler(false, false)
        handler.registerCommandLineListener(object : CommandLineListener {

            override fun onExited(exitCode: Int, exception: Throwable?) {
                e = exception
            }
        })

        val cmd = CommandLine(listOf("sleep", "5"))
        cmd.execute(handler, 2, TimeUnit.SECONDS).exception.shouldNotBeNull().shouldBeInstanceOf<TimeoutException>()
        e.shouldNotBeNull().shouldBeInstanceOf<TimeoutException>()
    }

    @Test
    fun executeWithWrite() {
        val lines = ArrayList<String>()
        val handler = CommandLineHandler()
        handler.registerCommandLineListener(object : CommandLineListener {

            override fun onLineRead(line: String) {
                lines.add(line)
            }
        })

        val cmd = CommandLine(listOf("/bin/bash", "-c", "read text && echo \$text"))
        thread { handler.write("1234567890") }
        cmd.execute(handler).exitCode shouldBeExactly 0

        lines shouldHaveSize 1 shouldContainAll listOf("1234567890")
    }

    @Test
    fun kill() {
        val handler = CommandLineHandler()
        val cmd = CommandLine(listOf("sleep", "5"))
        thread { Thread.sleep(1000); handler.kill() }
        measureTimeMillis { cmd.execute(handler) } shouldBeGreaterThanOrEqual 1000 shouldBeLessThan 5000
    }
}
