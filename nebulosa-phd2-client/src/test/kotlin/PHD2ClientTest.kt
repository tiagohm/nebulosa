import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import nebulosa.phd2.client.PHD2Client
import nebulosa.phd2.client.PHD2EventListener
import nebulosa.phd2.client.commands.*
import nebulosa.phd2.client.events.PHD2Event
import nebulosa.phd2.client.events.State

@EnabledIf(NonGitHubOnlyCondition::class)
class PHD2ClientTest : StringSpec(), PHD2EventListener {

    init {
        "start" {
            val client = PHD2Client("localhost")
            client.registerListener(this@PHD2ClientTest)
            client.run()

            delay(1000)

            val profiles = client.sendCommandAndGetResult(GetProfiles).shouldNotBeNull() shouldHaveAtLeastSize 1
            client.sendCommandAndGetResult(SetProfile(profiles[0]))
            client.sendCommandAndGetResult(GetProfile).shouldNotBeNull().id shouldBeExactly profiles[0].id
            client.sendCommandAndGetResult(GetAppState).shouldNotBeNull() shouldBe State.STOPPED
            client.sendCommandAndGetResult(SetConnected(true)).shouldNotBeNull()
            client.sendCommandAndGetResult(GetConnected).shouldNotBeNull().shouldBeTrue()
            client.sendCommandAndGetResult(ClearCalibration())
            client.sendCommandAndGetResult(GetCalibrated).shouldNotBeNull().shouldBeFalse()

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
