import io.kotest.core.spec.style.StringSpec
import nebulosa.guiding.phd2.EventListener
import nebulosa.guiding.phd2.PHD2Client
import nebulosa.guiding.phd2.event.PHD2Event

class PHD2ClientTest : StringSpec(), EventListener {

    init {
        "start" {
            val client = PHD2Client("pi.local")
            client.registerListener(this@PHD2ClientTest)
            client.start()

            Thread.sleep(70000)

            client.close()
        }
    }

    override fun onEvent(client: PHD2Client, event: PHD2Event) {
        println(event)
    }
}
