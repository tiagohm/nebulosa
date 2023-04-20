package nebulosa.desktop.logic.atlas.provider.catalog

import nebulosa.desktop.model.DsoEntity
import nebulosa.desktop.model.NameEntity
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Velocity.Companion.auDay
import nebulosa.skycatalog.DSO
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
        if (radius.value <= 0.0) return emptyList()

        return transaction {
            addLogger(DSOCatalogProvider)

            val names = NameEntity.name.groupConcat(GROUP_CONCAT_SEPARATOR).alias("names")

            DsoEntity
                .join(NameEntity, JoinType.INNER, additionalConstraint = { DsoEntity.id eq NameEntity.dso })
                .slice(names, *DEEP_SKY_OBJECTS_COLUMNS)
                .select { distance(DsoEntity.rightAscension, DsoEntity.declination, rightAscension, declination, radius) }
                .groupBy(DsoEntity.id)
                .limit(1000)
                .map { it.makeDSO(names) }
        }
    }

    override fun searchBy(name: String): List<DSO> {
        if (name.isBlank()) return emptyList()

        return transaction {
            addLogger(DSOCatalogProvider)

            val names = NameEntity.name.groupConcat(GROUP_CONCAT_SEPARATOR).alias("names")

            DsoEntity
                .join(NameEntity, JoinType.INNER, additionalConstraint = { (DsoEntity.id eq NameEntity.dso) and (NameEntity.name like name) })
                .slice(names, *DEEP_SKY_OBJECTS_COLUMNS)
                .selectAll()
                .groupBy(DsoEntity.id)
                .limit(1000)
                .map { it.makeDSO(names) }
        }
    }

    companion object : SqlLogger {

        @JvmStatic private val LOG = LoggerFactory.getLogger(DSOCatalogProvider::class.java)
        @JvmStatic private val DEEP_SKY_OBJECTS_COLUMNS = DsoEntity.columns.toTypedArray()

        private const val GROUP_CONCAT_SEPARATOR = ":"

        override fun log(context: StatementContext, transaction: Transaction) {
            LOG.info(context.expandArgs(transaction))
        }

        @JvmStatic
        private fun ResultRow.makeDSO(names: Expression<String>): DSO {
            return DSO(
                id = this[DsoEntity.id],
                names = this[names].split(GROUP_CONCAT_SEPARATOR),
                m = this[DsoEntity.m],
                ngc = this[DsoEntity.ngc],
                ic = this[DsoEntity.ic],
                c = this[DsoEntity.c],
                b = this[DsoEntity.b],
                sh2 = this[DsoEntity.sh2],
                vdb = this[DsoEntity.vdb],
                rcw = this[DsoEntity.rcw],
                ldn = this[DsoEntity.ldn],
                lbn = this[DsoEntity.lbn],
                cr = this[DsoEntity.cr],
                mel = this[DsoEntity.mel],
                pgc = this[DsoEntity.pgc],
                ugc = this[DsoEntity.ugc],
                arp = this[DsoEntity.arp],
                vv = this[DsoEntity.vv],
                dwb = this[DsoEntity.dwb],
                tr = this[DsoEntity.tr],
                st = this[DsoEntity.st],
                ru = this[DsoEntity.ru],
                vdbha = this[DsoEntity.vdbha],
                ced = this[DsoEntity.ced],
                pk = this[DsoEntity.pk],
                png = this[DsoEntity.png],
                snrg = this[DsoEntity.snrg],
                aco = this[DsoEntity.aco],
                hcg = this[DsoEntity.hcg],
                eso = this[DsoEntity.eso],
                vdbh = this[DsoEntity.vdbh],
                mB = this[DsoEntity.mB],
                mV = this[DsoEntity.mV],
                rightAscension = this[DsoEntity.rightAscension].rad,
                declination = this[DsoEntity.declination].rad,
                type = this[DsoEntity.type],
                mType = this[DsoEntity.mType],
                majorAxis = this[DsoEntity.majorAxis].rad,
                minorAxis = this[DsoEntity.minorAxis].rad,
                orientation = this[DsoEntity.orientation].rad,
                redshift = this[DsoEntity.redshift],
                parallax = this[DsoEntity.parallax].rad,
                radialVelocity = this[DsoEntity.radialVelocity].auDay,
                distance = this[DsoEntity.distance],
                pmRA = this[DsoEntity.pmRA].rad,
                pmDEC = this[DsoEntity.pmDEC].rad,
                constellation = this[DsoEntity.constellation],
            )
        }
    }
}
