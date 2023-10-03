import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import nebulosa.phd2.client.PHD2Client
import nebulosa.phd2.client.PHD2EventListener
import nebulosa.phd2.client.commands.CaptureSingleFrame
import nebulosa.phd2.client.commands.ClearCalibration
import nebulosa.phd2.client.commands.Dither
import nebulosa.phd2.client.events.PHD2Event

@EnabledIf(NonGitHubOnlyCondition::class)
class PHD2ClientTest : StringSpec(), PHD2EventListener {

    init {
        "start" {
            val client = PHD2Client("localhost")
            client.registerListener(this@PHD2ClientTest)
            client.run()

            delay(2000)

            client.sendCommand(ClearCalibration(true))
                .whenComplete { value, e -> e?.printStackTrace(); println(value) }

            client.sendCommand(CaptureSingleFrame())
                .whenComplete { value, e -> e?.printStackTrace(); println(value) }

            client.sendCommand(Dither(10.0))
                .whenComplete { value, e -> e?.printStackTrace(); println(value) }

            delay(15000)

            client.close()
        }
    }

    override fun onEvent(event: PHD2Event) {
        println(event)
    }
}
