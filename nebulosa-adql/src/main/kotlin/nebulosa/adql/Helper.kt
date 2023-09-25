@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.adql

import adql.query.operand.ADQLColumn
import adql.query.constraint.Between as ADQLBetween
import adql.query.constraint.In as ADQLIn
import adql.query.constraint.IsNull as ADQLIsNull

inline val Double.operand
    get() = Literal(this)

inline val Float.operand
    get() = Literal(this)

inline val Long.operand
    get() = Literal(this)

inline val Int.operand
    get() = Literal(this)

inline val String.operand
    get() = Literal(this)

inline val Operand<ADQLColumn>.isNull
    get() = IsNull(this)

inline val Operand<ADQLColumn>.isNotNull
    get() = IsNotNull(this)

// EQUAL

inline infix fun Operand<*>.equal(other: Operand<*>): Equal {
    return Equal(this, other)
}

inline infix fun Operand<*>.equal(other: Double): Equal {
    return this equal other.operand
}

inline infix fun Operand<*>.equal(other: Float): Equal {
    return this equal other.operand
}

inline infix fun Operand<*>.equal(other: Long): Equal {
    return this equal other.operand
}

inline infix fun Operand<*>.equal(other: Int): Equal {
    return this equal other.operand
}

inline infix fun Operand<*>.equal(other: String): Equal {
    return this equal other.operand
}

// NOT EQUAL

inline infix fun Operand<*>.notEqual(other: Operand<*>): NotEqual {
    return NotEqual(this, other)
}

inline infix fun Operand<*>.notEqual(other: Double): NotEqual {
    return this notEqual other.operand
}

inline infix fun Operand<*>.notEqual(other: Float): NotEqual {
    return this notEqual other.operand
}

inline infix fun Operand<*>.notEqual(other: Long): NotEqual {
    return this notEqual other.operand
}

inline infix fun Operand<*>.notEqual(other: Int): NotEqual {
    return this notEqual other.operand
}

inline infix fun Operand<*>.notEqual(other: String): NotEqual {
    return this notEqual other.operand
}

// BETWEEN

inline infix fun Operand<*>.between(range: ClosedFloatingPointRange<Double>): Between {
    return Between(this, range.start.operand, range.endInclusive.operand)
}

@JvmName("betweenFloat")
inline infix fun Operand<*>.between(range: ClosedFloatingPointRange<Float>): Between {
    return Between(this, range.start.operand, range.endInclusive.operand)
}

inline infix fun Operand<*>.between(range: LongRange): Between {
    return Between(this, range.first.operand, range.last.operand)
}

inline infix fun Operand<*>.between(range: IntRange): Between {
    return Between(this, range.first.operand, range.last.operand)
}

// NOT BETWEEN

inline infix fun Operand<*>.notBetween(range: ClosedFloatingPointRange<Double>): NotBetween {
    return NotBetween(this, range.start.operand, range.endInclusive.operand)
}

@JvmName("notBetweenFloat")
inline infix fun Operand<*>.notBetween(range: ClosedFloatingPointRange<Float>): NotBetween {
    return NotBetween(this, range.start.operand, range.endInclusive.operand)
}

inline infix fun Operand<*>.notBetween(range: LongRange): NotBetween {
    return NotBetween(this, range.first.operand, range.last.operand)
}

inline infix fun Operand<*>.notBetween(range: IntRange): NotBetween {
    return NotBetween(this, range.first.operand, range.last.operand)
}

// LESS THAN

inline infix fun Operand<*>.lessThan(other: Operand<*>): LessThan {
    return LessThan(this, other)
}

inline infix fun Operand<*>.lessThan(other: Double): LessThan {
    return this lessThan other.operand
}

inline infix fun Operand<*>.lessThan(other: Float): LessThan {
    return this lessThan other.operand
}

inline infix fun Operand<*>.lessThan(other: Long): LessThan {
    return this lessThan other.operand
}

inline infix fun Operand<*>.lessThan(other: Int): LessThan {
    return this lessThan other.operand
}

// LESS OR EQUAL

inline infix fun Operand<*>.lessOrEqual(other: Operand<*>): LessOrEqual {
    return LessOrEqual(this, other)
}

inline infix fun Operand<*>.lessOrEqual(other: Double): LessOrEqual {
    return this lessOrEqual other.operand
}

inline infix fun Operand<*>.lessOrEqual(other: Float): LessOrEqual {
    return this lessOrEqual other.operand
}

inline infix fun Operand<*>.lessOrEqual(other: Long): LessOrEqual {
    return this lessOrEqual other.operand
}

inline infix fun Operand<*>.lessOrEqual(other: Int): LessOrEqual {
    return this lessOrEqual other.operand
}

// GREATER THAN

inline infix fun Operand<*>.greaterThan(other: Operand<*>): GreaterThan {
    return GreaterThan(this, other)
}

inline infix fun Operand<*>.greaterThan(other: Double): GreaterThan {
    return this greaterThan other.operand
}

inline infix fun Operand<*>.greaterThan(other: Float): GreaterThan {
    return this greaterThan other.operand
}

inline infix fun Operand<*>.greaterThan(other: Long): GreaterThan {
    return this greaterThan other.operand
}

inline infix fun Operand<*>.greaterThan(other: Int): GreaterThan {
    return this greaterThan other.operand
}

// GREATER OR EQUAL

inline infix fun Operand<*>.greaterOrEqual(other: Operand<*>): GreaterOrEqual {
    return GreaterOrEqual(this, other)
}

inline infix fun Operand<*>.greaterOrEqual(other: Double): GreaterOrEqual {
    return this greaterOrEqual other.operand
}

inline infix fun Operand<*>.greaterOrEqual(other: Float): GreaterOrEqual {
    return this greaterOrEqual other.operand
}

inline infix fun Operand<*>.greaterOrEqual(other: Long): GreaterOrEqual {
    return this greaterOrEqual other.operand
}

inline infix fun Operand<*>.greaterOrEqual(other: Int): GreaterOrEqual {
    return this greaterOrEqual other.operand
}

// LIKE

inline infix fun Operand<*>.like(other: Operand<*>): Like {
    return Like(this, other)
}

inline infix fun Operand<*>.like(other: String): Like {
    return this like other.operand
}

// NOT LIKE

inline infix fun Operand<*>.notLike(other: Operand<*>): NotLike {
    return NotLike(this, other)
}

inline infix fun Operand<*>.notLike(other: String): NotLike {
    return this notLike other.operand
}

// AND/OR

inline infix fun WhereConstraint.and(other: WhereConstraint): And {
    return And(this, other)
}

inline infix fun WhereConstraint.or(other: WhereConstraint): Or {
    return Or(this, other)
}

// GEOMETRY

inline infix fun Region.contains(other: Region): Contains {
    return Contains(this, other)
}

inline infix fun Region.notContains(other: Region): NotContains {
    return NotContains(this, other)
}

inline infix fun SkyPoint.distance(other: SkyPoint): Distance {
    return Distance(this, other)
}

// ADQL

internal inline operator fun ADQLBetween.not(): ADQLBetween {
    return ADQLBetween(leftOperand, minOperand, maxOperand, !isNotBetween)
}

internal inline operator fun ADQLIsNull.not(): ADQLIsNull {
    return ADQLIsNull(column, !isNotNull)
}

internal inline operator fun ADQLIn.not(): ADQLIn {
    return ADQLIn(operand, valuesList, !isNotIn)
}
