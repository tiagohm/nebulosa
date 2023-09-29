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

@JvmName("equal")
inline infix fun Operand<*>.equal(other: Operand<*>): Equal {
    return Equal(this, other)
}

@JvmName("equalDouble")
inline infix fun Operand<*>.equal(other: Double): Equal {
    return this equal other.operand
}

@JvmName("equalFloat")
inline infix fun Operand<*>.equal(other: Float): Equal {
    return this equal other.operand
}

@JvmName("equalLong")
inline infix fun Operand<*>.equal(other: Long): Equal {
    return this equal other.operand
}

@JvmName("equalInt")
inline infix fun Operand<*>.equal(other: Int): Equal {
    return this equal other.operand
}

@JvmName("equalString")
inline infix fun Operand<*>.equal(other: String): Equal {
    return this equal other.operand
}

// NOT EQUAL

@JvmName("notEqual")
inline infix fun Operand<*>.notEqual(other: Operand<*>): NotEqual {
    return NotEqual(this, other)
}

@JvmName("notEqualDouble")
inline infix fun Operand<*>.notEqual(other: Double): NotEqual {
    return this notEqual other.operand
}

@JvmName("notEqualFloat")
inline infix fun Operand<*>.notEqual(other: Float): NotEqual {
    return this notEqual other.operand
}

@JvmName("notEqualLong")
inline infix fun Operand<*>.notEqual(other: Long): NotEqual {
    return this notEqual other.operand
}

@JvmName("notEqualInt")
inline infix fun Operand<*>.notEqual(other: Int): NotEqual {
    return this notEqual other.operand
}

@JvmName("notEqualString")
inline infix fun Operand<*>.notEqual(other: String): NotEqual {
    return this notEqual other.operand
}

// BETWEEN

@JvmName("betweenDoubleRange")
inline infix fun Operand<*>.between(range: ClosedFloatingPointRange<Double>): Between {
    return Between(this, range.start.operand, range.endInclusive.operand)
}

@JvmName("betweenFloatRange")
inline infix fun Operand<*>.between(range: ClosedFloatingPointRange<Float>): Between {
    return Between(this, range.start.operand, range.endInclusive.operand)
}

@JvmName("betweenLongRange")
inline infix fun Operand<*>.between(range: LongRange): Between {
    return Between(this, range.first.operand, range.last.operand)
}

@JvmName("betweenIntRange")
inline infix fun Operand<*>.between(range: IntRange): Between {
    return Between(this, range.first.operand, range.last.operand)
}

// NOT BETWEEN

@JvmName("notBetweenDoubleRange")
inline infix fun Operand<*>.notBetween(range: ClosedFloatingPointRange<Double>): NotBetween {
    return NotBetween(this, range.start.operand, range.endInclusive.operand)
}

@JvmName("notBetweenFloatRange")
inline infix fun Operand<*>.notBetween(range: ClosedFloatingPointRange<Float>): NotBetween {
    return NotBetween(this, range.start.operand, range.endInclusive.operand)
}

@JvmName("notBetweenLongRange")
inline infix fun Operand<*>.notBetween(range: LongRange): NotBetween {
    return NotBetween(this, range.first.operand, range.last.operand)
}

@JvmName("notBetweenIntRange")
inline infix fun Operand<*>.notBetween(range: IntRange): NotBetween {
    return NotBetween(this, range.first.operand, range.last.operand)
}

// LESS THAN

@JvmName("lessThan")
inline infix fun Operand<*>.lessThan(other: Operand<*>): LessThan {
    return LessThan(this, other)
}

@JvmName("lessThanDouble")
inline infix fun Operand<*>.lessThan(other: Double): LessThan {
    return this lessThan other.operand
}

@JvmName("lessThanFloat")
inline infix fun Operand<*>.lessThan(other: Float): LessThan {
    return this lessThan other.operand
}

@JvmName("lessThanLong")
inline infix fun Operand<*>.lessThan(other: Long): LessThan {
    return this lessThan other.operand
}

@JvmName("lessThanInt")
inline infix fun Operand<*>.lessThan(other: Int): LessThan {
    return this lessThan other.operand
}

// LESS OR EQUAL

@JvmName("lessOrEqual")
inline infix fun Operand<*>.lessOrEqual(other: Operand<*>): LessOrEqual {
    return LessOrEqual(this, other)
}

@JvmName("lessOrEqualDouble")
inline infix fun Operand<*>.lessOrEqual(other: Double): LessOrEqual {
    return this lessOrEqual other.operand
}

@JvmName("lessOrEqualFloat")
inline infix fun Operand<*>.lessOrEqual(other: Float): LessOrEqual {
    return this lessOrEqual other.operand
}

@JvmName("lessOrEqualLong")
inline infix fun Operand<*>.lessOrEqual(other: Long): LessOrEqual {
    return this lessOrEqual other.operand
}

@JvmName("lessOrEqualInt")
inline infix fun Operand<*>.lessOrEqual(other: Int): LessOrEqual {
    return this lessOrEqual other.operand
}

// GREATER THAN

@JvmName("greaterThan")
inline infix fun Operand<*>.greaterThan(other: Operand<*>): GreaterThan {
    return GreaterThan(this, other)
}

@JvmName("greaterThanDouble")
inline infix fun Operand<*>.greaterThan(other: Double): GreaterThan {
    return this greaterThan other.operand
}

@JvmName("greaterThanFloat")
inline infix fun Operand<*>.greaterThan(other: Float): GreaterThan {
    return this greaterThan other.operand
}

@JvmName("greaterThanLong")
inline infix fun Operand<*>.greaterThan(other: Long): GreaterThan {
    return this greaterThan other.operand
}

@JvmName("greaterThanInt")
inline infix fun Operand<*>.greaterThan(other: Int): GreaterThan {
    return this greaterThan other.operand
}

// GREATER OR EQUAL

@JvmName("greaterOrEqual")
inline infix fun Operand<*>.greaterOrEqual(other: Operand<*>): GreaterOrEqual {
    return GreaterOrEqual(this, other)
}

@JvmName("greaterOrEqualDouble")
inline infix fun Operand<*>.greaterOrEqual(other: Double): GreaterOrEqual {
    return this greaterOrEqual other.operand
}

@JvmName("greaterOrEqualFloat")
inline infix fun Operand<*>.greaterOrEqual(other: Float): GreaterOrEqual {
    return this greaterOrEqual other.operand
}

@JvmName("greaterOrEqualLong")
inline infix fun Operand<*>.greaterOrEqual(other: Long): GreaterOrEqual {
    return this greaterOrEqual other.operand
}

@JvmName("greaterOrEqualInt")
inline infix fun Operand<*>.greaterOrEqual(other: Int): GreaterOrEqual {
    return this greaterOrEqual other.operand
}

// LIKE

@JvmName("like")
inline infix fun Operand<*>.like(other: Operand<*>): Like {
    return Like(this, other)
}

@JvmName("likeString")
inline infix fun Operand<*>.like(other: String): Like {
    return this like other.operand
}

// NOT LIKE

@JvmName("notLike")
inline infix fun Operand<*>.notLike(other: Operand<*>): NotLike {
    return NotLike(this, other)
}

@JvmName("notLikeString")
inline infix fun Operand<*>.notLike(other: String): NotLike {
    return this notLike other.operand
}

// IN

@JvmName("includes")
inline infix fun Operand<*>.includes(other: Array<out Operand<*>>): In {
    return In(this, other)
}

@JvmName("includes")
inline infix fun Operand<*>.includes(other: Iterable<Operand<*>>): In {
    return In(this, other)
}

@JvmName("includesListOfString")
inline infix fun Operand<*>.includes(other: List<String>): In {
    return this includes Array(other.size) { other[it].operand }
}

@JvmName("includesArrayOfString")
inline infix fun Operand<*>.includes(other: Array<String>): In {
    return this includes Array(other.size) { other[it].operand }
}

@JvmName("includesIntArray")
inline infix fun Operand<*>.includes(other: IntArray): In {
    return this includes Array(other.size) { other[it].operand }
}

@JvmName("includesLongArray")
inline infix fun Operand<*>.includes(other: LongArray): In {
    return this includes Array(other.size) { other[it].operand }
}

// AND/OR

@JvmName("and")
inline infix fun WhereConstraint.and(other: WhereConstraint): And {
    return And(this, other)
}

@JvmName("or")
inline infix fun WhereConstraint.or(other: WhereConstraint): Or {
    return Or(this, other)
}

// GEOMETRY

@JvmName("contains")
inline infix fun Column.contains(other: Region): Contains {
    return Contains(this, other)
}

@JvmName("contains")
inline infix fun Region.contains(other: Region): Contains {
    return Contains(this, other)
}

@JvmName("notContains")
inline infix fun Column.notContains(other: Region): NotContains {
    return NotContains(this, other)
}

@JvmName("notContains")
inline infix fun Region.notContains(other: Region): NotContains {
    return NotContains(this, other)
}

@JvmName("distance")
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
