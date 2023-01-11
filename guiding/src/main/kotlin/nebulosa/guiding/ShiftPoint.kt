package nebulosa.guiding

@Suppress("NOTHING_TO_INLINE")
class ShiftPoint : Point {

    /**
     * Rate of change (per second).
     */
    val rate = ZERO

    private var x0 = 0f
    private var y0 = 0f
    private var t0 = 0L

    constructor(x: Float, y: Float) : super(x, y)

    constructor(point: Point) : super(point)

    fun updateXY(x: Float, y: Float) {
        this.x = x
        this.y = y
        beginShift()
    }

    fun setShiftRate(x: Float, y: Float) {
        rate.x = x
        rate.y = y
        beginShift()
    }

    fun beginShift() {
        if (isValid) {
            x0 = x
            y0 = y
            t0 = System.currentTimeMillis()
        }
    }

    inline fun disableShift() {
        rate.invalidate()
    }

    fun updateShift() {
        if (isValid && rate.isValid) {
            val dt = (System.currentTimeMillis() - t0) / 1000f
            x = x0 + rate.x * dt
            y = y0 + rate.y * dt
        }
    }
}
