package nebulosa.api.mounts

import nebulosa.api.confirmation.ConfirmationEvent

data class MountWillPointToBelowHorizonEvent(override val idempotencyKey: String) : ConfirmationEvent {

    override val message =
        "Pointing a telescope below the horizon can damage the equipment and compromise the stability of your setup. Do you want to proceed?"
}
