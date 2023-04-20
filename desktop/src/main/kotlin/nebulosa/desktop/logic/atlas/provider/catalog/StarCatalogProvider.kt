package nebulosa.desktop.logic.atlas.provider.catalog

import nebulosa.desktop.model.NameEntity
import nebulosa.desktop.model.StarEntity
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Velocity.Companion.auDay
import nebulosa.skycatalog.Star
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class StarCatalogProvider : AbstractCatalogProvider<Star>() {

    override fun searchAround(
        rightAscension: Angle,
        declination: Angle,
        radius: Angle,
    ): List<Star> {
        if (radius.value <= 0.0) return emptyList()

        return transaction {
            addLogger(StarCatalogProvider)

            val names = NameEntity.name.groupConcat(GROUP_CONCAT_SEPARATOR).alias("names")

            StarEntity
                .join(NameEntity, JoinType.INNER, additionalConstraint = { StarEntity.id eq NameEntity.star })
                .slice(names, *STARS_COLUMNS)
                .select { distance(StarEntity.rightAscension, StarEntity.declination, rightAscension, declination, radius) }
                .groupBy(StarEntity.id)
                .limit(1000)
                .map { it.makeStar(names) }
        }
    }

    override fun searchBy(name: String): List<Star> {
        if (name.isBlank()) return emptyList()

        return transaction {
            addLogger(StarCatalogProvider)

            val names = NameEntity.name.groupConcat(GROUP_CONCAT_SEPARATOR).alias("names")

            StarEntity
                .join(NameEntity, JoinType.INNER, additionalConstraint = { (StarEntity.id eq NameEntity.star) and (NameEntity.name like name) })
                .slice(names, *STARS_COLUMNS)
                .selectAll()
                .groupBy(StarEntity.id)
                .limit(1000)
                .map { it.makeStar(names) }
        }
    }

    companion object : SqlLogger {

        @JvmStatic private val LOG = LoggerFactory.getLogger(StarCatalogProvider::class.java)
        @JvmStatic private val STARS_COLUMNS = StarEntity.columns.toTypedArray()

        private const val GROUP_CONCAT_SEPARATOR = ":"

        override fun log(context: StatementContext, transaction: Transaction) {
            LOG.info(context.expandArgs(transaction))
        }

        @JvmStatic
        private fun ResultRow.makeStar(names: Expression<String>): Star {
            return Star(
                id = this[StarEntity.id],
                names = this[names].split(GROUP_CONCAT_SEPARATOR),
                hr = this[StarEntity.hr],
                hd = this[StarEntity.hd],
                hip = this[StarEntity.hip],
                sao = this[StarEntity.sao],
                mB = this[StarEntity.mB],
                mV = this[StarEntity.mV],
                rightAscension = this[StarEntity.rightAscension].rad,
                declination = this[StarEntity.declination].rad,
                spType = this[StarEntity.spType],
                redshift = this[StarEntity.redshift],
                parallax = this[StarEntity.parallax].rad,
                radialVelocity = this[StarEntity.radialVelocity].auDay,
                distance = this[StarEntity.distance],
                pmRA = this[StarEntity.pmRA].rad,
                pmDEC = this[StarEntity.pmDEC].rad,
                type = this[StarEntity.type],
                constellation = this[StarEntity.constellation],
            )
        }
    }
}
