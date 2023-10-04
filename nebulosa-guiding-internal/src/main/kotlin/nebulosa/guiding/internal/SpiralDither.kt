package nebulosa.guiding.internal

class SpiralDither : Dither {

    private var prevRaOnly = false
    private var x = 0
    private var y = 0
    private var dx = 0
    private var dy = 0

    override fun get(amount: Double, raOnly: Boolean): DoubleArray {
        if (raOnly != prevRaOnly) {
            reset()
            prevRaOnly = raOnly
        }

        return if (raOnly) {
            dy = -dx.also { dx = dy }

            val x0 = x

            if (dy == 0) x = -x
            else x += dy

            doubleArrayOf((x - x0) * amount, 0.0)
        } else {
            if (x == y || (x > 0 && x == -y) || (x <= 0 && y == 1 - x)) {
                dy = -dx.also { dx = dy }
            }

            x += dx
            y += dy

            doubleArrayOf(dx * amount, dy * amount)
        }
    }

    override fun reset() {
        x = 0
        y = 0
        dx = -1
        dy = 0
        prevRaOnly = false
    }
}
