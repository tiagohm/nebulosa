package nebulosa.indi.device

import nebulosa.indi.protocol.SwitchElement
import nebulosa.indi.protocol.SwitchVector

@Suppress("NOTHING_TO_INLINE")
inline fun <E : SwitchElement> SwitchVector<E>.firstOnSwitch() = elements.first { it.value }

@Suppress("NOTHING_TO_INLINE")
inline fun <E : SwitchElement> SwitchVector<E>.firstOnSwitchOrNull() = elements.firstOrNull { it.value }
