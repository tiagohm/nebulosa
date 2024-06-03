import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldNotBeExactly
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import nebulosa.common.exec.LineReadListener
import nebulosa.common.exec.commandLine
import nebulosa.test.NonGitHubOnlyCondition
import java.nio.file.Path
import java.time.Duration
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

@EnabledIf(NonGitHubOnlyCondition::class)
class CommandLineTest : StringSpec() {

    init {
        "sleep" {
            val cmd = commandLine {
                executable("sleep")
                putArg("2")
            }

            measureTimeMillis {
                cmd.start().get() shouldBeExactly 0
            } shouldBeGreaterThanOrEqual 2000
        }
        "sleep with timeout" {
            val cmd = commandLine {
                executable("sleep")
                putArg("10")
            }

            measureTimeMillis {
                cmd.start(Duration.ofSeconds(2)).get() shouldNotBeExactly 0
            } shouldBeGreaterThanOrEqual 2000
        }
        "kill sleep" {
            val cmd = commandLine {
                executable("sleep")
                putArg("10")
            }

            thread { Thread.sleep(2000); cmd.stop() }

            measureTimeMillis {
                cmd.start().get() shouldNotBeExactly 0
            } shouldBeGreaterThanOrEqual 2000 shouldBeLessThan 10000
        }
        "ls" {
            val lineReadListener = object : LineReadListener.OnInput, ArrayList<String>() {

                override fun onInputRead(line: String) {
                    add(line)
                }
            }

            val cmd = commandLine {
                executable("ls")
                workingDirectory(Path.of("../"))
                registerLineReadListener(lineReadListener)
            }

            cmd.start().get() shouldBeExactly 0
            lineReadListener.shouldNotBeEmpty()
            lineReadListener.shouldContain("nebulosa-image")
        }
    }
}
