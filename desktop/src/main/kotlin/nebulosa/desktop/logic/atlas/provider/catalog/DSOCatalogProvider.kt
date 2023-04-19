package nebulosa.desktop.logic.atlas.provider.catalog

import nebulosa.desktop.model.DeepSkyObjects
import nebulosa.desktop.model.Names
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Velocity.Companion.auDay
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.DSO
import nebulosa.skycatalog.SkyObjectType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("dsoCatalogProvider")
class DSOCatalogProvider : AbstractCatalogProvider<DSO>() {

    override fun searchAround(
        rightAscension: Angle,
        declination: Angle,
        radius: Angle,
    ): List<DSO> {
        fun ResultRow.filterByDistance(): Boolean {
            return CatalogProvider
                .distanceBetween(
                    this[DeepSkyObjects.rightAscension], this[DeepSkyObjects.declination],
                    rightAscension.value, declination.value
                ) <= radius.value
        }

        return transaction {
            addLogger(DSOCatalogProvider)

            DeepSkyObjects
                .selectAll()
                .filter { it.filterByDistance() }
                .map { it.makeDSO() }
        }
    }

    override fun searchBy(name: String): List<DSO> {
        return transaction {
            addLogger(DSOCatalogProvider)

            DeepSkyObjects
                .join(Names, JoinType.INNER, additionalConstraint = { (DeepSkyObjects.id eq Names.dso) and (Names.name like name) })
                .slice(DeepSkyObjects.columns)
                .selectAll()
                .withDistinct()
                .limit(1000)
                .map { it.makeDSO() }
        }
    }

    companion object : SqlLogger {

        @JvmStatic private val LOG = LoggerFactory.getLogger(DSOCatalogProvider::class.java)

        override fun log(context: StatementContext, transaction: Transaction) {
            if (LOG.isDebugEnabled) {
                LOG.debug(context.expandArgs(transaction))
            }
        }

        @JvmStatic
        private fun ResultRow.makeDSO(): DSO {
            val names = Names
                .select { Names.dso eq this@makeDSO[DeepSkyObjects.id] }
                .map { it[Names.name] }

            return DSO(
                id = this[DeepSkyObjects.id],
                names = names,
                m = this[DeepSkyObjects.m],
                ngc = this[DeepSkyObjects.ngc],
                ic = this[DeepSkyObjects.ic],
                c = this[DeepSkyObjects.c],
                b = this[DeepSkyObjects.b],
                sh2 = this[DeepSkyObjects.sh2],
                vdb = this[DeepSkyObjects.vdb],
                rcw = this[DeepSkyObjects.rcw],
                ldn = this[DeepSkyObjects.ldn],
                lbn = this[DeepSkyObjects.lbn],
                cr = this[DeepSkyObjects.cr],
                mel = this[DeepSkyObjects.mel],
                pgc = this[DeepSkyObjects.pgc],
                ugc = this[DeepSkyObjects.ugc],
                arp = this[DeepSkyObjects.arp],
                vv = this[DeepSkyObjects.vv],
                dwb = this[DeepSkyObjects.dwb],
                tr = this[DeepSkyObjects.tr],
                st = this[DeepSkyObjects.st],
                ru = this[DeepSkyObjects.ru],
                vdbha = this[DeepSkyObjects.vdbha],
                ced = this[DeepSkyObjects.ced],
                pk = this[DeepSkyObjects.pk],
                png = this[DeepSkyObjects.png],
                snrg = this[DeepSkyObjects.snrg],
                aco = this[DeepSkyObjects.aco],
                hcg = this[DeepSkyObjects.hcg],
                eso = this[DeepSkyObjects.eso],
                vdbh = this[DeepSkyObjects.vdbh],
                mB = this[DeepSkyObjects.mB],
                mV = this[DeepSkyObjects.mV],
                rightAscension = this[DeepSkyObjects.rightAscension].rad,
                declination = this[DeepSkyObjects.declination].rad,
                type = SkyObjectType.valueOf(this[DeepSkyObjects.type]),
                mType = this[DeepSkyObjects.mType],
                majorAxis = this[DeepSkyObjects.majorAxis].rad,
                minorAxis = this[DeepSkyObjects.minorAxis].rad,
                orientation = this[DeepSkyObjects.orientation].rad,
                redshift = this[DeepSkyObjects.redshift],
                parallax = this[DeepSkyObjects.parallax].rad,
                radialVelocity = this[DeepSkyObjects.radialVelocity].auDay,
                distance = this[DeepSkyObjects.distance],
                pmRA = this[DeepSkyObjects.pmRA].rad,
                pmDEC = this[DeepSkyObjects.pmDEC].rad,
                constellation = Constellation.valueOf(this[DeepSkyObjects.constellation]),
            )
        }
    }
}
