package nebulosa.watney.stardetector

import kotlin.math.hypot

/**
 * A class representing a "bin" of star pixels, i.e. the list of a single star's pixels
 * in an image.
 */
class StarPixelBin {

    @JvmField internal val pixelRows = HashMap<Int, MutableList<StarPixel>>()

    var left = Int.MAX_VALUE
        private set

    var right = Int.MIN_VALUE
        private set

    var top = Int.MAX_VALUE
        private set

    var bottom = Int.MIN_VALUE
        private set

    val pixelCount
        get() = pixelRows.values.sumOf { it.size }

    val dimensions
        get() = intArrayOf(right - left + 1, pixelRows.size)

    fun add(x: Int, y: Int, value: Float) {
        if (x < left) left = x
        if (x > right) right = x
        if (y < top) top = y
        if (y > bottom) bottom = y

        val pixel = StarPixel(x, y, value)
        pixelRows.getOrPut(y, ::ArrayList).add(pixel)
    }

    fun recalculateBounds() {
        left = Int.MAX_VALUE
        right = Int.MIN_VALUE
        top = Int.MAX_VALUE
        bottom = Int.MIN_VALUE

        for ((key, value) in pixelRows) {
            if (key < top) top = key
            if (key > bottom) bottom = key

            for ((x) in value) {
                if (x < left) left = x
                if (x > right) right = x
            }
        }
    }

    // https://www.gaia.ac.uk/sites/default/files/resources/Calculating_Magnitudes.pdf
    fun computeCenterPixelPosAndRelativeBrightness(): Star {
        // Center coordinate in small stars is generally the brightest pixel in the bin.
        // When there are more pixels of the same or almost the same brightness, we will calculate
        // their center point.

        val pCount = pixelCount

        val starPixelHeight = bottom - top
        val starPixelWidth = right - left
        val starSize = hypot(starPixelHeight.toDouble(), starPixelWidth.toDouble())

        // With small stars just settle with the center of the canvas.
        if (pCount <= 9) {
            val starPosY = top + 0.5 * (bottom - top)
            val starPosX = left + 0.5 * (right - left)
            return Star(starPosX, starPosY, starSize)
        }

        var l = Int.MAX_VALUE
        var r = Int.MIN_VALUE
        var t = Int.MAX_VALUE
        var b = Int.MIN_VALUE

        // Select 100% of the pixels ordered by brightness and just settle with the center.
        // Probably good enough approximation.
        for (row in pixelRows) {
            for ((x, y) in row.value) {
                if (x < l) l = x
                if (x > r) r = x
                if (y < t) t = y
                if (y > b) b = y
            }
        }

        val starPosY = t + 0.5 * (b - t)
        val starPosX = l + 0.5 * (r - l)

        return Star(starPosX, starPosY, starSize)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StarPixelBin) return false

        if (left != other.left) return false
        if (right != other.right) return false
        if (top != other.top) return false
        if (bottom != other.bottom) return false

        return true
    }

    override fun hashCode(): Int {
        var result = left
        result = 31 * result + right
        result = 31 * result + top
        result = 31 * result + bottom
        return result
    }
}
