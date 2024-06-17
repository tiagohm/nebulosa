package nebulosa.api.notifications

import nebulosa.api.messages.MessageEvent
import nebulosa.api.messages.QueueableEvent

interface NotificationEvent : MessageEvent {

    val target: String?

    val severity: Severity

    val title: String?
        get() = severity.name

    val body: String

    val data: Any?
        get() = null

    override val eventName
        get() = "NOTIFICATION.SENT"

    interface Info : NotificationEvent {

        override val severity
            get() = Severity.INFO
    }

    interface Success : NotificationEvent {

        override val severity
            get() = Severity.SUCCESS
    }

    interface Warning : NotificationEvent {

        override val severity
            get() = Severity.WARNING
    }

    interface Error : NotificationEvent {

        override val severity
            get() = Severity.ERROR
    }

    interface System : NotificationEvent, QueueableEvent {

        override val target
            get() = null
    }

    interface Window : NotificationEvent {

        override val target: String
    }
}
