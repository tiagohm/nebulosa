@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.database

import org.jetbrains.exposed.sql.BooleanColumnType
import org.jetbrains.exposed.sql.ComparisonOp
import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.InternalApi
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryParameter
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.arrayParam
import org.jetbrains.exposed.sql.resolveColumnType
import org.jetbrains.exposed.sql.stringParam

@PublishedApi
internal class ILikeOp(a: Expression<*>, b: Expression<*>) : ComparisonOp(a, b, "ILIKE")

inline infix fun <T : String?> ExpressionWithColumnType<T>.ilike(pattern: String): Op<Boolean> = ILikeOp(this, stringParam(pattern))

@PublishedApi
internal class ArrayContains(a: Expression<*>, b: Expression<*>) : CustomFunction<Boolean>("ARRAY_CONTAINS", BooleanColumnType(), a, b)

@OptIn(InternalApi::class)
inline infix fun <reified T : Any> ExpressionWithColumnType<List<T>>.contains(value: T): Op<Boolean> =
    ArrayContains(this, QueryParameter(value, resolveColumnType(T::class))) eq true

inline infix fun <reified T : Any> ExpressionWithColumnType<List<T>>.contains(value: List<T>): Op<Boolean> =
    ArrayContains(this, arrayParam(value)) eq true
