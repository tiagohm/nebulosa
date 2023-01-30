package nebulosa.guiding.phd2

import nebulosa.guiding.phd2.event.PHD2Event

fun interface EventListener {

    fun onEvent(client: PHD2Client, event: PHD2Event)
}
