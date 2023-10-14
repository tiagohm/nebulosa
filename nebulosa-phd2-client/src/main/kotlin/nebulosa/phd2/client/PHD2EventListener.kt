package nebulosa.phd2.client

import nebulosa.phd2.client.commands.PHD2Command
import nebulosa.phd2.client.events.PHD2Event

interface PHD2EventListener {

    fun onEventReceived(event: PHD2Event)

    fun <T> onCommandProcessed(command: PHD2Command<T>, result: T?, error: String?)
}
