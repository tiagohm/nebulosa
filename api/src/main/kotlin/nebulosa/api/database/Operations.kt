package nebulosa.api.database

import org.jetbrains.exposed.sql.*

internal class ILikeOp(a: Expression<*>, b: Expression<*>) : ComparisonOp(a, b, "ILIKE")

infix fun ExpressionWithColumnType<String?>.ilike(pattern: String): Op<Boolean> = ILikeOp(this, QueryParameter(pattern, columnType))
