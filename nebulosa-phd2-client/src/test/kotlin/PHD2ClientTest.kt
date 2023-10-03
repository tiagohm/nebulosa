import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import nebulosa.phd2.client.PHD2Client
import nebulosa.phd2.client.PHD2EventListener
import nebulosa.phd2.client.commands.*
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

            client.sendCommand(GetAppState)
                .whenComplete { value, e -> e?.printStackTrace(); println(value) }

            client.sendCommand(GetCalibrated)
                .whenComplete { value, e -> e?.printStackTrace(); println(value) }

            client.sendCommand(GetConnected)
                .whenComplete { value, e -> e?.printStackTrace(); println(value) }

            client.sendCommand(GetCalibrationData)
                .whenComplete { value, e -> e?.printStackTrace(); println(value) }

            client.sendCommand(GetCameraFrameSize)
                .whenComplete { value, e -> e?.printStackTrace(); println(value.contentToString()) }

            client.sendCommand(GetAlgorithmParamNames("ra"))
                .whenComplete { value, e -> e?.printStackTrace(); println(value.contentToString()) }

            client.sendCommand(GetAlgorithmParamNames("dec"))
                .whenComplete { value, e -> e?.printStackTrace(); println(value.contentToString()) }

            client.sendCommand(GetAlgorithmParam("ra", "algorithmName"))
                .whenComplete { value, e -> e?.printStackTrace(); println(value) }

            client.sendCommand(GetAlgorithmParam("dec", "algorithmName"))
                .whenComplete { value, e -> e?.printStackTrace(); println(value) }

            client.sendCommand(FindStar())
                .whenComplete { value, e -> e?.printStackTrace(); println(value.contentToString()) }

            client.sendCommand(CaptureSingleFrame())
                .whenComplete { value, e -> e?.printStackTrace(); println(value) }

            client.sendCommand(Dither(10.0))
                .whenComplete { value, e -> e?.printStackTrace(); println(value) }

            delay(15000)

            client.close()
        }
    }

    override fun onEventReceived(event: PHD2Event) {
        println(event)
    }

    override fun <T> onCommandProcessed(command: PHD2Command<T>, result: T?, error: String?) {
        println("$command, $result, $error")
    }
}
