package nebulosa.api.notification

import nebulosa.api.services.MessageEvent

interface NotificationEvent : MessageEvent {

    enum class Severity {
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
    }

    val type: String

    val body: String

    val severity
        get() = Severity.INFO

    val title: String?
        get() = severity.name

    val silent
        get() = false

    override val eventName
        get() = "NOTIFICATION"
}
