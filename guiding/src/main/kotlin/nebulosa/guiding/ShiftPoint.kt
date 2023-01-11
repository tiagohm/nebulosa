package nebulosa.guiding

@Suppress("NOTHING_TO_INLINE")
class ShiftPoint : Point {

    /**
     * Rate of change (per second).
     */
    val rate = ZERO

    private var x0 = 0
    private var y0 = 0
    private var t0 = 0L

    constructor(x: Int, y: Int) : super(x, y)

    constructor(point: Point) : super(point)

    inline fun updateXY(x: Int, y: Int) {
        this.x = x
        this.y = y
        beginShift()
    }

    inline fun updateShiftRate(x: Int, y: Int) {
        rate.x = x
        rate.y = y
        beginShift()
    }

    fun beginShift() {
        if (valid) {
            x0 = x
            y0 = y
            t0 = System.currentTimeMillis()
        }
    }

    inline fun disableShift() {
        rate.invalidate()
    }

    fun updateShift() {
        if (valid && rate.valid) {
            val dt = (System.currentTimeMillis() - t0) / 1000f
            x = (x0 + rate.x * dt).toInt()
            y = (y0 + rate.y * dt).toInt()
        }
    }
}
