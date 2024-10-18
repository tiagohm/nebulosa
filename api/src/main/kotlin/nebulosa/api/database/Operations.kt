@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.api.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

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
