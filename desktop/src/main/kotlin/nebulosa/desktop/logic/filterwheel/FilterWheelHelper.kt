package nebulosa.desktop.logic.filterwheel

import nebulosa.desktop.service.PreferenceService
import nebulosa.indi.device.filterwheel.FilterWheel

fun PreferenceService.filterName(filterWheel: FilterWheel, position: Int = filterWheel.position): String {
    val label = string("filterWheel.${filterWheel.name}.filter.$position.label") ?: ""
    return label.ifEmpty { "Filter #$position" }
}
