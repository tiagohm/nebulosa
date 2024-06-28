package nebulosa.api.atlas.ephemeris

import nebulosa.horizons.HorizonsElement
import nebulosa.nova.position.GeographicPosition
import java.time.LocalDateTime

sealed interface EphemerisProvider<T : Any> {

    fun compute(
        target: T, position: GeographicPosition,
        dateTime: LocalDateTime, offsetInSeconds: Long, fully: Boolean,
    ): List<HorizonsElement>
}
