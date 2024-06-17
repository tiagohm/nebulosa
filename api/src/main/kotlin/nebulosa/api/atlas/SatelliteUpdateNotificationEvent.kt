package nebulosa.api.atlas

import nebulosa.api.notifications.NotificationEvent

sealed interface SatelliteUpdateNotificationEvent : NotificationEvent.System {

    data object Started : SatelliteUpdateNotificationEvent, NotificationEvent.Info {

        override val body = "Satellite database is being updated"
    }

    data class Finished(private val numberOfSatellites: Int) : SatelliteUpdateNotificationEvent, NotificationEvent.Success {

        override val body = "Satellite database was updated: $numberOfSatellites satellites"
    }
}
