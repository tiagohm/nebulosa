package nebulosa.api.focusers

import nebulosa.indi.device.focuser.FocuserEvent

fun interface FocuserEventAware {

    fun handleFocuserEvent(event: FocuserEvent)
}
