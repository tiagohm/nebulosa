package nebulosa.api.focusers

import nebulosa.indi.device.focuser.Focuser
import org.springframework.stereotype.Service
import java.util.*

@Service
class FocuserService : Vector<Focuser>(2) {

    operator fun get(name: String): Focuser? {
        return firstOrNull { it.name == name }
    }

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

    fun syncTo(focuser: Focuser, steps: Int) {
        focuser.syncFocusTo(steps)
    }
}
