package nebulosa.guiding.phd2

import nebulosa.guiding.Guider
import nebulosa.phd2.client.PHD2Client
import nebulosa.phd2.client.PHD2EventListener
import nebulosa.phd2.client.commands.PHD2Command
import nebulosa.phd2.client.events.PHD2Event
import java.io.Closeable

class PHD2Guider(private val client: PHD2Client) : Guider, PHD2EventListener, Closeable {

    init {
        client.registerListener(this)
    }

    override fun onEventReceived(event: PHD2Event) {

    }

    override fun <T> onCommandProcessed(command: PHD2Command<T>, result: T?, error: String?) {

    }

    override fun close() {
        client.unregisterListener(this)
        client.close()
    }
}
