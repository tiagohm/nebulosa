package nebulosa.api.repositories

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder.StringOrder.CASE_INSENSITIVE
import io.objectbox.query.QueryBuilder.StringOrder.CASE_SENSITIVE
import io.objectbox.query.QueryFilter
import nebulosa.api.data.entities.StarEntity
import nebulosa.api.data.entities.StarEntity_
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.util.zip.GZIPInputStream
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.measureTimeMillis

@Service
class StarRepository(
    boxStore: BoxStore,
    private val objectMapper: ObjectMapper,
) : BoxRepository<StarEntity>() {

    override val box = boxStore.boxFor(StarEntity::class.java)!!

    fun search(
        text: String? = null,
        rightAscension: Angle = Angle.ZERO, declination: Angle = Angle.ZERO, radius: Angle = Angle.ZERO,
        constellation: Constellation? = null,
        magnitudeMin: Double = -SkyObject.UNKNOWN_MAGNITUDE, magnitudeMax: Double = SkyObject.UNKNOWN_MAGNITUDE,
        type: SkyObjectType? = null,
    ): List<StarEntity> {
        return box.query()
            .let { if (text.isNullOrBlank()) it else it.contains(StarEntity_.names, text, CASE_INSENSITIVE) }
            .let { if (constellation == null) it else it.equal(StarEntity_.constellation, constellation.name, CASE_SENSITIVE) }
            .let { if (type == null) it else it.equal(StarEntity_.type, type.name, CASE_SENSITIVE) }
            .between(StarEntity_.magnitude, magnitudeMin, magnitudeMax)
            .let { if (radius.value <= 0.0) it else it.filter(RightAscensionDeclinationQueryFilter(rightAscension, declination, radius)) }
            .order(StarEntity_.magnitude)
            .build()
            .use { if (radius.value <= 0.0) it.find(0, 1000) else it.find() }
    }

    fun load(resource: Resource) {
        deleteAll()

        val elapsedTime = measureTimeMillis {
            GZIPInputStream(resource.inputStream)
                .use { objectMapper.readValue(it, object : TypeReference<List<StarEntity>>() {}) }
                .onEach { it.id = 0 }
                .also(box::put)
        }

        LOG.info("star database loaded. elapsedTime={} ms", elapsedTime)
    }

    private class RightAscensionDeclinationQueryFilter(
        private val rightAscension: Angle, declination: Angle, private val radius: Angle,
    ) : QueryFilter<StarEntity> {

        private val sinDEC = declination.sin
        private val cosDEC = declination.cos

        override fun keep(entity: StarEntity): Boolean {
            return acos(sin(entity.declination) * sinDEC + cos(entity.declination) * cosDEC * cos(entity.rightAscension - rightAscension.value)) <= radius.value
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<StarRepository>()
    }
}
