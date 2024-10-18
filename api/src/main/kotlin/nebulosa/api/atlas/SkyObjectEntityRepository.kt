package nebulosa.api.atlas

import nebulosa.math.Angle
import nebulosa.math.cos
import nebulosa.math.sin
import nebulosa.math.toDegrees
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.functions.math.ACosFunction
import org.jetbrains.exposed.sql.functions.math.CosFunction
import org.jetbrains.exposed.sql.functions.math.SinFunction
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class SkyObjectEntityRepository(private val connection: Database) {

    operator fun get(id: Long) = transaction(connection) {
        SkyObjectTable
            .selectAll()
            .where { SkyObjectTable.id eq id }
            .firstOrNull()
            ?.let(SkyObjectEntity::from)
    }

    fun search(
        name: String? = null, constellation: Constellation? = null,
        rightAscension: Angle = 0.0, declination: Angle = 0.0, radius: Angle = 0.0,
        magnitudeMin: Double = SkyObject.MAGNITUDE_MIN, magnitudeMax: Double = SkyObject.MAGNITUDE_MAX,
        type: SkyObjectType? = null, id: Long = 0L,
    ) = transaction(connection) {
        val findByRegion = radius > 0.0 && radius.toDegrees in 0.016..90.0 // 1 arcmin to 90 deg

        SkyObjectTable
            .selectAll()
            .also { if (id > 0L) it.andWhere { SkyObjectTable.id eq id } }
            .also { if (magnitudeMin > SkyObject.MAGNITUDE_MIN && magnitudeMin < SkyObject.MAGNITUDE_MAX) it.andWhere { SkyObjectTable.magnitude greaterEq magnitudeMin } }
            .also { if (magnitudeMax > SkyObject.MAGNITUDE_MIN && magnitudeMax < SkyObject.MAGNITUDE_MAX) it.andWhere { SkyObjectTable.magnitude lessEq magnitudeMax } }
            .also { if (type != null) it.andWhere { SkyObjectTable.type eq type } }
            .also { if (constellation != null) it.andWhere { SkyObjectTable.constellation eq constellation } }
            .also { if (!name.isNullOrBlank()) it.andWhere { SkyObjectTable.name like "%$name%" } }
            .also {
                if (findByRegion) {
                    val sinDEC = BigDecimal(declination.sin)
                    val cosDEC = BigDecimal(declination.cos)

                    // acos(sin(o.declinationJ2000) * sinDEC + cos(o.declinationJ2000) * cosDEC * cos(o.rightAscensionJ2000 - rightAscension)) <= radius
                    it.andWhere { ACosFunction((SinFunction(SkyObjectTable.declination) times sinDEC) plus (CosFunction(SkyObjectTable.declination) times cosDEC times CosFunction(SkyObjectTable.rightAscension minus rightAscension))) lessEq BigDecimal(radius) }
                }
            }
            .orderBy(SkyObjectTable.magnitude)
            .limit(5000)
            .map(SkyObjectEntity::from)
    }

    private val count = Count(SkyObjectTable.id)

    val size
        get() = transaction(connection) { SkyObjectTable.select(count).first()[count] }

    val objectTypes
        get() = transaction(connection) {
            SkyObjectTable
                .select(SkyObjectTable.type)
                .distinct()
                .map { it[SkyObjectTable.type] }
                .toSortedSet()
        }

    fun add(entity: SkyObjectEntity) = transaction(connection) {
        entity.id = SkyObjectTable
            .insert { entity.mapTo(it) } get SkyObjectTable.id
        entity
    }

    fun clear() = transaction(connection) {
        SkyObjectTable.deleteAll() > 0
    }
}
