package nebulosa.api.services

import jakarta.annotation.PostConstruct
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserAttached
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.focuser.FocuserEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Service

@Service
class FocuserService(
    private val webSocketService: WebSocketService,
    private val eventBus: EventBus,
) {

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFocuserEvent(event: FocuserEvent) {
        when (event) {
            is PropertyChangedEvent -> webSocketService.sendFocuserUpdated(event.device!!)
            is FocuserAttached -> webSocketService.sendFocuserAttached(event.device)
            is FocuserDetached -> webSocketService.sendFocuserDetached(event.device)
        }
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
