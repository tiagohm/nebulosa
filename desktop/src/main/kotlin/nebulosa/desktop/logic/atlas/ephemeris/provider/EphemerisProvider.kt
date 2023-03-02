package nebulosa.desktop.logic.atlas.ephemeris.provider

import nebulosa.horizons.HorizonsEphemeris
import nebulosa.nova.position.GeographicPosition

sealed interface EphemerisProvider<in T> {

    fun compute(
        target: T,
        position: GeographicPosition,
        force: Boolean = false,
    ): HorizonsEphemeris?
}
