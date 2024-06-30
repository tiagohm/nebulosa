package nebulosa.api.confirmation

import nebulosa.api.message.MessageEvent

interface ConfirmationEvent : MessageEvent {

    val message: String

    val idempotencyKey: String

    override val eventName
        get() = "CONFIRMATION"
}
