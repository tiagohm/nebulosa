import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import nebulosa.phd2.client.PHD2Client
import nebulosa.phd2.client.PHD2EventListener
import nebulosa.phd2.client.commands.PHD2Command
import nebulosa.phd2.client.events.PHD2Event

@EnabledIf(NonGitHubOnlyCondition::class)
class PHD2ClientTest : StringSpec(), PHD2EventListener {

    init {
        "start" {
            val client = PHD2Client()
            client.registerListener(this@PHD2ClientTest)
            client.open("localhost", PHD2Client.DEFAULT_PORT)

            delay(1000)

            client.close()
        }
    }

    override fun onEventReceived(event: PHD2Event) {
        println(event)
    }

    override fun <T> onCommandProcessed(command: PHD2Command<T>, result: T?, error: String?) {
        // println("$command, $result, $error")
    }

    private fun <T> PHD2Client.sendCommandAndGetResult(command: PHD2Command<T>): T? {
        return try {
            val result = sendCommandSync(command)
            if (result is Array<*>) println(result.contentToString())
            else println(result)
            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
