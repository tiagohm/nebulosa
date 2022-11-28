package nebulosa.indi.devices

import nebulosa.indi.protocol.SwitchElement
import nebulosa.indi.protocol.SwitchState
import nebulosa.indi.protocol.SwitchVector

@Suppress("NOTHING_TO_INLINE")
inline fun SwitchElement.isOn() = value == SwitchState.ON

@Suppress("NOTHING_TO_INLINE")
inline fun SwitchElement.isOff() = value == SwitchState.OFF

@Suppress("NOTHING_TO_INLINE")
inline fun <E : SwitchElement> SwitchVector<E>.firstOnSwitch() = elements.first(SwitchElement::isOn)
