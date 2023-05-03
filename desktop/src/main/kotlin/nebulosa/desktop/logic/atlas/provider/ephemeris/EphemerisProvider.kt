package nebulosa.desktop.logic.atlas.provider.ephemeris

import nebulosa.horizons.HorizonsEphemeris
import nebulosa.horizons.HorizonsQuantity
import nebulosa.nova.position.GeographicPosition
import java.time.LocalDate

sealed interface EphemerisProvider<in T> {

    fun compute(
        target: T,
        position: GeographicPosition,
        date: LocalDate = LocalDate.now(),
        force: Boolean = false,
        vararg quantities: HorizonsQuantity,
    ): HorizonsEphemeris?
}
