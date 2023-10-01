package nebulosa.api.focusers

import nebulosa.indi.device.focuser.Focuser
import org.springframework.stereotype.Service

@Service
class FocuserService {

    fun connect(focuser: Focuser) {
        focuser.connect()
    }

    fun disconnect(focuser: Focuser) {
        focuser.disconnect()
    }

    fun moveIn(focuser: Focuser, steps: Int) {
        focuser.moveFocusIn(steps)
    }

    fun moveOut(focuser: Focuser, steps: Int) {
        focuser.moveFocusOut(steps)
    }

    fun moveTo(focuser: Focuser, steps: Int) {
        focuser.moveFocusTo(steps)
    }

    fun abort(focuser: Focuser) {
        focuser.abortFocus()
    }

    fun sync(focuser: Focuser, steps: Int) {
        focuser.syncFocusTo(steps)
    }
}
