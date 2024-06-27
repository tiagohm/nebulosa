package nebulosa.api.atlas

import nebulosa.api.notification.NotificationEvent

sealed interface SkyAtlasUpdateNotificationEvent : NotificationEvent.System {

    data object Started : SkyAtlasUpdateNotificationEvent, NotificationEvent.Info {

        override val body = "Sky Atlas database is being updated"
    }

    data class Finished(private val version: String) : SkyAtlasUpdateNotificationEvent, NotificationEvent.Success {

        override val body = "Sky Atlas database was updated: $version"
    }

    data object Failed : SkyAtlasUpdateNotificationEvent, NotificationEvent.Error {

        override val body = "Sky Atlas database update failed"
    }
}
