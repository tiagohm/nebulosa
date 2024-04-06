package nebulosa.api.atlas

import io.objectbox.Box
import io.objectbox.kotlin.equal
import io.objectbox.query.QueryBuilder.StringOrder.CASE_INSENSITIVE
import io.objectbox.query.QueryFilter
import nebulosa.api.repositories.BoxRepository
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectInsideCoordinate
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
        val useFilter = radius > 0.0 && radius.toDegrees > 0.1

        return box.query()
            .also {
                if (magnitudeMin in SkyObject.MAGNITUDE_RANGE) it.greaterOrEqual(SimbadEntity_.magnitude, magnitudeMin)
                if (magnitudeMax in SkyObject.MAGNITUDE_RANGE) it.lessOrEqual(SimbadEntity_.magnitude, magnitudeMax)
                if (type != null) it.equal(SimbadEntity_.type, type.ordinal)
                if (constellation != null) it.equal(SimbadEntity_.constellation, constellation.ordinal)

                if (!name.isNullOrBlank()) {
                    it.contains(SimbadEntity_.name, name, CASE_INSENSITIVE)
                }

                if (useFilter) it.filter(object : QueryFilter<SimbadEntity> {
                    private val filter = SkyObjectInsideCoordinate(rightAscension, declination, radius)

                    override fun keep(entity: SimbadEntity) = filter.test(entity)
                })

                it.order(SimbadEntity_.magnitude)
            }
            .build()
            .use { if (useFilter) it.find() else it.find(0, 5000) }
    }
}
