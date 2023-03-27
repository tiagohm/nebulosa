package nebulosa.desktop.logic.guider

import javafx.beans.property.SimpleBooleanProperty
import nebulosa.desktop.logic.AbstractDeviceProperty
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.guide.GuideOutputPulsingChanged

open class DefaultGuideOutputProperty : AbstractDeviceProperty<GuideOutput>(), GuideOutputProperty {

    override val canPulseGuideProperty = SimpleBooleanProperty()
    override val pulseGuidingProperty = SimpleBooleanProperty()

    override fun onChanged(prev: GuideOutput?, device: GuideOutput) {
        canPulseGuideProperty.set(device.canPulseGuide)
        pulseGuidingProperty.set(device.pulseGuiding)
    }

    override fun onReset() {
        canPulseGuideProperty.set(false)
        pulseGuidingProperty.set(false)
    }

    override fun onDeviceEvent(event: DeviceEvent<*>, device: GuideOutput) {
        when (event) {
            is GuideOutputPulsingChanged -> pulseGuidingProperty.set(device.pulseGuiding)
        }
    }
}
