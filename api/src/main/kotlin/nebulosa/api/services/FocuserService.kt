package nebulosa.api.services

import jakarta.annotation.PostConstruct
import nebulosa.api.data.responses.FocuserResponse
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.focuser.FocuserAttached
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.focuser.FocuserEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Service

@Service
class FocuserService(
    private val equipmentService: EquipmentService,
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

    fun attachedFocusers(): List<FocuserResponse> {
        return equipmentService.focusers().map(::FocuserResponse)
    }

    operator fun get(name: String): FocuserResponse {
        val focuser = requireNotNull(equipmentService.focuser(name))
        return FocuserResponse(focuser)
    }

    fun connect(name: String) {
        val focuser = requireNotNull(equipmentService.focuser(name))
        focuser.connect()
    }

    fun disconnect(name: String) {
        val focuser = requireNotNull(equipmentService.focuser(name))
        focuser.disconnect()
    }

    fun moveIn(name: String, steps: Int) {
        val focuser = requireNotNull(equipmentService.focuser(name))
        focuser.moveFocusIn(steps)
    }

    fun moveOut(name: String, steps: Int) {
        val focuser = requireNotNull(equipmentService.focuser(name))
        focuser.moveFocusOut(steps)
    }

    fun moveTo(name: String, steps: Int) {
        val focuser = requireNotNull(equipmentService.focuser(name))
        focuser.moveFocusTo(steps)
    }

    fun abort(name: String) {
        val focuser = requireNotNull(equipmentService.focuser(name))
        focuser.abortFocus()
    }

    fun syncTo(name: String, steps: Int) {
        val focuser = requireNotNull(equipmentService.focuser(name))
        focuser.syncFocusTo(steps)
    }
}
