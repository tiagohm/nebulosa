package nebulosa.api.atlas

import io.objectbox.Box
import io.objectbox.kotlin.equal
import nebulosa.api.repositories.BoxRepository
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class SimbadEntityRepository(@Qualifier("simbadBox") override val box: Box<SimbadEntity>) : BoxRepository<SimbadEntity>() {

    fun find(
        name: String? = null, constellation: Constellation? = null,
        rightAscension: Angle = 0.0, declination: Angle = 0.0, radius: Angle = 0.0,
        magnitudeMin: Double = SkyObject.MAGNITUDE_MIN, magnitudeMax: Double = SkyObject.MAGNITUDE_MAX,
        type: SkyObjectType? = null,
    ): List<SimbadEntity> {
        val useFilter = radius > 0.0 && radius.toDegrees in 0.1..90.0

        val condition = and(
            if (magnitudeMin in SkyObject.MAGNITUDE_RANGE) SimbadEntity_.magnitude.greaterOrEqual(magnitudeMin) else null,
            if (magnitudeMax in SkyObject.MAGNITUDE_RANGE) SimbadEntity_.magnitude.lessOrEqual(magnitudeMax) else null,
            if (type != null) SimbadEntity_.type equal type.ordinal else null,
            if (constellation != null) SimbadEntity_.constellation equal constellation.ordinal else null,
            if (name.isNullOrBlank()) null else SimbadEntity_.name containsInsensitive name,
        )

        return with(condition?.let(box::query) ?: box.query()) {
            if (useFilter) filter(SkyObjectInsideCoordinate(rightAscension, declination, radius))
            order(SimbadEntity_.magnitude)
            build()
        }.use { if (useFilter) it.find() else it.find(0, 5000) }
    }
}
