package nebulosa.desktop.logic.atlas.provider.catalog

import nebulosa.math.Angle
import nebulosa.skycatalog.SkyObject
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.times

abstract class AbstractCatalogProvider<out T : SkyObject> : CatalogProvider<T> {

    companion object {

        @JvmStatic
        fun Expression<*>.groupConcat(separator: String): CustomFunction<String> {
            return CustomFunction("GROUP_CONCAT", TextColumnType(), this, stringParam(separator))
        }

        @JvmStatic
        fun Expression<*>.sin(): CustomFunction<Double> {
            return CustomFunction("SIN", DoubleColumnType(), this)
        }

        @JvmStatic
        fun Expression<*>.cos(): CustomFunction<Double> {
            return CustomFunction("COS", DoubleColumnType(), this)
        }

        @JvmStatic
        fun Expression<*>.acos(): CustomFunction<Double> {
            return CustomFunction("ACOS", DoubleColumnType(), this)
        }

        @JvmStatic
        fun distance(
            rightAscensionColumn: ExpressionWithColumnType<Double>, declinationColumn: ExpressionWithColumnType<Double>,
            rightAscension: Angle, declination: Angle,
            radius: Angle,
        ): Op<Boolean> {
            // acos(sin(d.dec) * sin(DEC) + cos(d.dec) * cos(DEC) * cos(d.rightAscension - RA)) <= RADIUS
            return ((declinationColumn.sin() * declination.sin) +
                    (declinationColumn.cos() * declination.cos * (rightAscensionColumn - rightAscension.value).cos())).acos() lessEq radius.value
        }
    }
}
