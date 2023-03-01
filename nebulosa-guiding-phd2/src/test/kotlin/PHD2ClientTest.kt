import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.StringSpec
import nebulosa.phd2.client.PHD2EventListener
import nebulosa.phd2.client.PHD2Client
import nebulosa.phd2.client.event.PHD2Event

@Ignored
@Suppress("BlockingMethodInNonBlockingContext")
class PHD2ClientTest : StringSpec(), PHD2EventListener {

    init {
        "start" {
            val client = PHD2Client("pi.local")
            client.registerListener(this@PHD2ClientTest)
            client.connect()

            Thread.sleep(70000)

            client.close()
        }
    }

    override fun onEvent(client: PHD2Client, event: PHD2Event) {
        println(event)
    }
}
