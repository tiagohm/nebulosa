package nebulosa.api.atlas

import java.time.LocalDateTime

data class MoonPhaseDateTime(
    @JvmField val dateTime: LocalDateTime = LocalDateTime.MIN,
    @JvmField val name: MoonPhase = MoonPhase.NEW_MOON,
)
