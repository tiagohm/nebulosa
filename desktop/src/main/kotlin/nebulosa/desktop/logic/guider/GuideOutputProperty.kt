package nebulosa.desktop.logic.guider

import javafx.beans.property.SimpleBooleanProperty
import nebulosa.desktop.logic.DeviceProperty
import nebulosa.indi.device.guide.GuideOutput

interface GuideOutputProperty : DeviceProperty<GuideOutput> {

    val canPulseGuideProperty: SimpleBooleanProperty

    val pulseGuidingProperty: SimpleBooleanProperty

    val canPulseGuide
        get() = canPulseGuideProperty.get()

    val pulseGuiding
        get() = pulseGuidingProperty.get()
}
