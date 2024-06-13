package nebulosa.api.atlas

import io.objectbox.query.QueryFilter
import nebulosa.math.Angle
import nebulosa.math.cos
import nebulosa.math.sin
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

data class SkyObjectInsideCoordinate(
    private val rightAscension: Angle,
    private val declination: Angle,
    private val radius: Angle,
) : QueryFilter<SimbadEntity> {

    private val sinDEC = declination.sin
    private val cosDEC = declination.cos

    override fun keep(o: SimbadEntity): Boolean {
        return acos(sin(o.declinationJ2000) * sinDEC + cos(o.declinationJ2000) * cosDEC * cos(o.rightAscensionJ2000 - rightAscension)) <= radius
    }
}
