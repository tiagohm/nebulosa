package nebulosa.api.atlas

import java.time.LocalDateTime

data class EarthSeasonDateTime(
    @JvmField val dateTime: LocalDateTime = LocalDateTime.MIN,
    @JvmField val name: EarthSeason = EarthSeason.MARCH_EQUINOX,
)
