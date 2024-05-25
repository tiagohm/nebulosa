package nebulosa.api.autofocus

import nebulosa.api.messages.MessageEvent

class AutoFocusEvent : MessageEvent {

    override val eventName = "AUTO_FOCUS.ELAPSED"
}
