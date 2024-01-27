package nebulosa.guiding.internal

import nebulosa.math.Point2D

/**
 * Represents a location on a guide camera image.
 */
@Suppress("NOTHING_TO_INLINE")
open class Point(
    x: Double = 0.0,
    y: Double = 0.0,
    valid: Boolean = true,
) : GuidePoint {

    override var valid = valid
        protected set

    override var x = x
        protected set

    override var y = y
        protected set

    constructor(point: Point) : this(point.x, point.y, point.valid)

    internal open fun set(x: Double, y: Double) {
        this.x = x
        this.y = y
        valid = true
    }

    internal inline fun set(point: Point2D) {
        set(point.x, point.y)
    }

    internal open fun invalidate() {
        valid = false
    }

    inline operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    inline operator fun minus(other: Point): Point {
        return Point(x - other.x, y - other.y)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Point) return false

        if (valid != other.valid) return false
        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = valid.hashCode()
        result = 31 * result + x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    override fun toString(): String {
        return "Point(valid=$valid, x=$x, y=$y)"
    }
}
