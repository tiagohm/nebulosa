package nebulosa.api.data.responses

import nebulosa.api.data.enums.SatelliteGroupType

data class SatelliteResponse(
    var id: Long = 0L,
    var name: String = "",
    var tle: String = "",
    val groups: MutableSet<SatelliteGroupType> = HashSet(1),
)
