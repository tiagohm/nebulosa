package nebulosa.api.notifications

import nebulosa.api.messages.MessageEvent
import nebulosa.api.messages.QueueableEvent

interface NotificationEvent : MessageEvent, QueueableEvent {

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
        get() = "NOTIFICATION.SENT"
}
