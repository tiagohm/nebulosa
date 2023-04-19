package nebulosa.desktop.logic.atlas.provider.catalog

import nebulosa.desktop.model.Names
import nebulosa.desktop.model.Stars
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Velocity.Companion.auDay
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
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
        fun ResultRow.filterByDistance(): Boolean {
            return CatalogProvider
                .distanceBetween(
                    this[Stars.rightAscension], this[Stars.declination],
                    rightAscension.value, declination.value
                ) <= radius.value
        }

        return transaction {
            addLogger(StarCatalogProvider)

            Stars
                .selectAll()
                .filter { it.filterByDistance() }
                .map { it.makeStar() }
        }
    }

    override fun searchBy(name: String): List<Star> {
        return transaction {
            addLogger(StarCatalogProvider)

            Stars
                .join(Names, JoinType.INNER, additionalConstraint = { (Stars.id eq Names.star) and (Names.name like name) })
                .slice(Stars.columns)
                .selectAll()
                .withDistinct()
                .limit(1000)
                .map { it.makeStar() }
        }
    }

    companion object : SqlLogger {

        @JvmStatic private val LOG = LoggerFactory.getLogger(StarCatalogProvider::class.java)

        override fun log(context: StatementContext, transaction: Transaction) {
            if (LOG.isDebugEnabled) {
                LOG.debug(context.expandArgs(transaction))
            }
        }

        @JvmStatic
        private fun ResultRow.makeStar(): Star {
            val names = Names
                .select { Names.star eq this@makeStar[Stars.id] }
                .map { it[Names.name] }

            return Star(
                id = this[Stars.id],
                names = names,
                hr = this[Stars.hr],
                hd = this[Stars.hd],
                hip = this[Stars.hip],
                sao = this[Stars.sao],
                mB = this[Stars.mB],
                mV = this[Stars.mV],
                rightAscension = this[Stars.rightAscension].rad,
                declination = this[Stars.declination].rad,
                spType = this[Stars.spType],
                redshift = this[Stars.redshift],
                parallax = this[Stars.parallax].rad,
                radialVelocity = this[Stars.radialVelocity].auDay,
                distance = this[Stars.distance],
                pmRA = this[Stars.pmRA].rad,
                pmDEC = this[Stars.pmDEC].rad,
                type = SkyObjectType.valueOf(this[Stars.type]),
                constellation = Constellation.valueOf(this[Stars.constellation]),
            )
        }
    }
}
