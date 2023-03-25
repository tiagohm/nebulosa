package nebulosa.guiding.internal

@Suppress("NOTHING_TO_INLINE")
class ShiftPoint : Point {

    /**
     * Rate of change (per second).
     */
    val rate = Point()

    private var x0 = 0.0
    private var y0 = 0.0
    private var t0 = 0L

    constructor(x: Double, y: Double) : super(x, y)

    constructor(point: Point) : super(point)

    override fun set(x: Double, y: Double) {
        super.set(x, y)
        beginShift()
    }

    fun shiftRate(x: Double, y: Double) {
        rate.set(x, y)
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
            x = x0 + rate.x * dt
            y = y0 + rate.y * dt
        }
    }
}
