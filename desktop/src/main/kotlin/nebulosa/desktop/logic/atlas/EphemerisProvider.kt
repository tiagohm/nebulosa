package nebulosa.desktop.logic.atlas

import nebulosa.nova.position.GeographicPosition
import nebulosa.query.horizons.HorizonsEphemeris

sealed interface EphemerisProvider<in T> {

    fun compute(
        target: T,
        position: GeographicPosition,
        force: Boolean = false,
    ): HorizonsEphemeris?
}
