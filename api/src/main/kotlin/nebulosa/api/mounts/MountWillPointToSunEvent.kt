package nebulosa.api.mounts

import nebulosa.api.confirmation.ConfirmationEvent

data class MountWillPointToSunEvent(override val idempotencyKey: String) : ConfirmationEvent {

    override val message =
        "Pointing a telescope directly at the Sun can cause severe eye injury or blindness and can damage the telescope. Do you want to proceed?"
}
