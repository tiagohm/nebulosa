package nebulosa.api.atlas

import nebulosa.api.notification.NotificationEvent

data class SkyAtlasUpdateFinished(override val body: String) : NotificationEvent {

    override val type = "SKY_ATLAS_UPDATE_FINISHED"
}
