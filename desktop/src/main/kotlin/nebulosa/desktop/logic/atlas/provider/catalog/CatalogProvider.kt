package nebulosa.desktop.logic.atlas.provider.catalog

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.skycatalog.SkyObject

interface CatalogProvider<out T : SkyObject> {

    fun searchAround(rightAscension: Angle, declination: Angle, radius: Angle = DEFAULT_RADIUS): List<T>

    fun searchBy(name: String): List<T>

    companion object {

        @JvmStatic val DEFAULT_RADIUS = 1.0.deg
    }
}
