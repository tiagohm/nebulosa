package nebulosa.desktop.logic.atlas.provider.catalog

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.skycatalog.SkyObject
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

interface CatalogProvider<out T : SkyObject> {

    fun searchAround(rightAscension: Angle, declination: Angle, radius: Angle = DEFAULT_RADIUS): List<T>

    fun searchBy(name: String): List<T>

    companion object {

        @JvmStatic val DEFAULT_RADIUS = 1.0.deg

        @JvmStatic
        fun distanceBetween(
            rightAscension1: Double, declination1: Double,
            rightAscension2: Double, declination2: Double,
        ): Double {
            return acos(sin(declination1) * sin(declination2) + cos(declination1) * cos(declination2) * cos(rightAscension1 - rightAscension2))
        }
    }
}
