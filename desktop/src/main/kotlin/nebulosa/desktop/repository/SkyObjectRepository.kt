package nebulosa.desktop.repository

import nebulosa.desktop.model.DsoEntity
import nebulosa.desktop.model.NameEntity
import nebulosa.desktop.model.SkyObjectEntity
import nebulosa.desktop.model.StarEntity
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Velocity.Companion.auDay
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.DSO
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import nebulosa.skycatalog.Star
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.times
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SkyObjectRepository {

    data class Filter(
        val rightAscension: Angle = Angle.ZERO,
        val declination: Angle = Angle.ZERO,
        val radius: Angle = Angle.ZERO,
        val constellation: Constellation? = null,
        val magnitudeMin: Double = Double.MAX_VALUE,
        val magnitudeMax: Double = Double.MAX_VALUE,
        val type: SkyObjectType? = null,
    ) {

        companion object {

            @JvmStatic val EMPTY = Filter()
        }
    }

    fun searchStar(
        text: String,
        filter: Filter,
    ): List<Star> {
        return StarEntity.search(
            text,
            filter.rightAscension, filter.declination, filter.radius,
            filter.constellation, filter.magnitudeMin, filter.magnitudeMax,
            filter.type, STARS_COLUMNS, NameEntity.star, ::makeStar,
        )
    }

    fun searchDSO(
        text: String,
        filter: Filter,
    ): List<DSO> {
        return DsoEntity.search(
            text,
            filter.rightAscension, filter.declination, filter.radius,
            filter.constellation, filter.magnitudeMin, filter.magnitudeMax,
            filter.type, DEEP_SKY_OBJECTS_COLUMNS, NameEntity.dso, ::makeDSO,
        )
    }

    private fun <T, R : SkyObject> T.search(
        text: String,
        rightAscension: Angle, declination: Angle, radius: Angle,
        constellation: Constellation?,
        magnitudeMin: Double, magnitudeMax: Double,
        type: SkyObjectType?,
        columns: Array<out Column<*>>,
        joinId: Column<Int?>,
        mapper: (ResultRow, Expression<String>) -> R,
    ): List<R> where T : SkyObjectEntity, T : Table {
        return transaction {
            addLogger(SkyObjectRepository)

            val entity = this@search

            val shouldUseFilter = radius.value > 0.0 ||
                    constellation != null || type != null ||
                    magnitudeMin in MAGNITUDE_RANGE ||
                    magnitudeMax in MAGNITUDE_RANGE

            if (!shouldUseFilter && text.isBlank()) {
                emptyList()
            } else {
                val names = NameEntity.name.groupConcat(GROUP_CONCAT_SEPARATOR).alias("names")

                val joinConstraint = (entity.id eq joinId)
                    .let { if (text.isBlank()) it else it and (NameEntity.name like text) }

                val select = ArrayList<Op<Boolean>>(4)

                if (radius.value > 0.0) {
                    select.add(distance(entity.rightAscension, entity.declination, rightAscension, declination, radius))
                }

                if (constellation != null) {
                    select.add(entity.constellation eq constellation)
                }

                if (type != null) {
                    select.add(entity.type eq type)
                }

                if (magnitudeMin in MAGNITUDE_RANGE || magnitudeMax in MAGNITUDE_RANGE) {
                    select.add(entity.magnitude.between(magnitudeMin, magnitudeMax))
                }

                entity
                    .join(NameEntity, JoinType.INNER, additionalConstraint = { joinConstraint })
                    .slice(names, *columns)
                    .let { if (select.isEmpty()) it.selectAll() else it.select { select.foldWithAnd() } }
                    .groupBy(entity.id)
                    .orderBy(entity.magnitude)
                    .limit(1000)
                    .map { mapper(it, names) }
            }
        }
    }

    companion object : SqlLogger {

        private const val GROUP_CONCAT_SEPARATOR = ":"

        @JvmStatic private val MAGNITUDE_RANGE = -29.9..29.9
        @JvmStatic private val LOG = LoggerFactory.getLogger(SkyObjectRepository::class.java)
        @JvmStatic private val STARS_COLUMNS = StarEntity.columns.toTypedArray()
        @JvmStatic private val DEEP_SKY_OBJECTS_COLUMNS = DsoEntity.columns.toTypedArray()

        override fun log(context: StatementContext, transaction: Transaction) {
            LOG.info(context.expandArgs(transaction))
        }

        @JvmStatic
        private fun List<Op<Boolean>>.foldWithAnd(): Op<Boolean> {
            var accumulator = first()
            for (i in 1 until size) accumulator = accumulator and this[i]
            return accumulator
        }

        @JvmStatic
        private fun Expression<*>.groupConcat(separator: String): CustomFunction<String> {
            return CustomFunction("GROUP_CONCAT", TextColumnType(), this, stringParam(separator))
        }

        @JvmStatic
        private fun Expression<*>.sin(): CustomFunction<Double> {
            return CustomFunction("SIN", DoubleColumnType(), this)
        }

        @JvmStatic
        private fun Expression<*>.cos(): CustomFunction<Double> {
            return CustomFunction("COS", DoubleColumnType(), this)
        }

        @JvmStatic
        private fun Expression<*>.acos(): CustomFunction<Double> {
            return CustomFunction("ACOS", DoubleColumnType(), this)
        }

        @JvmStatic
        private fun distance(
            rightAscensionColumn: ExpressionWithColumnType<Double>, declinationColumn: ExpressionWithColumnType<Double>,
            rightAscension: Angle, declination: Angle,
            radius: Angle,
        ): Op<Boolean> {
            // acos(sin(d.dec) * sin(DEC) + cos(d.dec) * cos(DEC) * cos(d.rightAscension - RA)) <= RADIUS
            return ((declinationColumn.sin() * declination.sin) +
                    (declinationColumn.cos() * declination.cos * (rightAscensionColumn - rightAscension.value).cos())).acos() lessEq radius.value
        }

        @JvmStatic
        private fun makeStar(row: ResultRow, names: Expression<String>): Star {
            return Star(
                id = row[StarEntity.id],
                names = row[names].split(GROUP_CONCAT_SEPARATOR),
                hr = row[StarEntity.hr],
                hd = row[StarEntity.hd],
                hip = row[StarEntity.hip],
                magnitude = row[StarEntity.magnitude],
                rightAscension = row[StarEntity.rightAscension].rad,
                declination = row[StarEntity.declination].rad,
                spType = row[StarEntity.spType],
                redshift = row[StarEntity.redshift],
                parallax = row[StarEntity.parallax].rad,
                radialVelocity = row[StarEntity.radialVelocity].auDay,
                distance = row[StarEntity.distance],
                pmRA = row[StarEntity.pmRA].rad,
                pmDEC = row[StarEntity.pmDEC].rad,
                type = row[StarEntity.type],
                constellation = row[StarEntity.constellation],
            )
        }

        @JvmStatic
        private fun makeDSO(row: ResultRow, names: Expression<String>): DSO {
            return DSO(
                id = row[DsoEntity.id],
                names = row[names].split(GROUP_CONCAT_SEPARATOR),
                m = row[DsoEntity.m],
                ngc = row[DsoEntity.ngc],
                ic = row[DsoEntity.ic],
                c = row[DsoEntity.c],
                b = row[DsoEntity.b],
                sh2 = row[DsoEntity.sh2],
                vdb = row[DsoEntity.vdb],
                rcw = row[DsoEntity.rcw],
                ldn = row[DsoEntity.ldn],
                lbn = row[DsoEntity.lbn],
                cr = row[DsoEntity.cr],
                mel = row[DsoEntity.mel],
                pgc = row[DsoEntity.pgc],
                ugc = row[DsoEntity.ugc],
                arp = row[DsoEntity.arp],
                vv = row[DsoEntity.vv],
                dwb = row[DsoEntity.dwb],
                tr = row[DsoEntity.tr],
                st = row[DsoEntity.st],
                ru = row[DsoEntity.ru],
                vdbha = row[DsoEntity.vdbha],
                ced = row[DsoEntity.ced],
                pk = row[DsoEntity.pk],
                png = row[DsoEntity.png],
                snrg = row[DsoEntity.snrg],
                aco = row[DsoEntity.aco],
                hcg = row[DsoEntity.hcg],
                eso = row[DsoEntity.eso],
                vdbh = row[DsoEntity.vdbh],
                magnitude = row[DsoEntity.magnitude],
                rightAscension = row[DsoEntity.rightAscension].rad,
                declination = row[DsoEntity.declination].rad,
                type = row[DsoEntity.type],
                mType = row[DsoEntity.mType],
                majorAxis = row[DsoEntity.majorAxis].rad,
                minorAxis = row[DsoEntity.minorAxis].rad,
                orientation = row[DsoEntity.orientation].rad,
                redshift = row[DsoEntity.redshift],
                parallax = row[DsoEntity.parallax].rad,
                radialVelocity = row[DsoEntity.radialVelocity].auDay,
                distance = row[DsoEntity.distance],
                pmRA = row[DsoEntity.pmRA].rad,
                pmDEC = row[DsoEntity.pmDEC].rad,
                constellation = row[DsoEntity.constellation],
            )
        }
    }
}
