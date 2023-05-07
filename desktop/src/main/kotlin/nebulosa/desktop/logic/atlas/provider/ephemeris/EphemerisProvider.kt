package nebulosa.desktop.logic.atlas.provider.ephemeris

import nebulosa.desktop.view.atlas.DateTimeProvider
import nebulosa.horizons.HorizonsEphemeris
import nebulosa.horizons.HorizonsQuantity
import nebulosa.nova.position.GeographicPosition

sealed interface EphemerisProvider<in T> {

    fun compute(
        target: T,
        position: GeographicPosition,
        dateTimeProvider: DateTimeProvider,
        force: Boolean = false,
        vararg quantities: HorizonsQuantity,
    ): HorizonsEphemeris?
}
