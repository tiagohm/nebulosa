package nebulosa.api.atlas

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class MoonPhaseDateTime(
    @JvmField @field:JsonFormat(shape = JsonFormat.Shape.NUMBER) val dateTime: LocalDateTime = LocalDateTime.MIN,
    @JvmField val name: MoonPhaseName = MoonPhaseName.NEW_MOON,
)
