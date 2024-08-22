package nebulosa.api.image

data class ROI(
    @JvmField val x: Int = 0, @JvmField val y: Int = 0,
    @JvmField val width: Int = 0, @JvmField val height: Int = 0,
) {

    fun constrained(maxWidth: Int, maxHeight: Int): ROI {
        val x1 = x.contraintTo(maxWidth)
        val y1 = y.contraintTo(maxHeight)
        val x2 = (x + width).contraintTo(maxWidth)
        val y2 = (y + height).contraintTo(maxHeight)
        val newWidth = x2 - x1
        val newHeight = y2 - y1

        return if (x1 != x || y1 != y || newWidth != width || newHeight != height) {
            ROI(x1, y1, newWidth, newHeight)
        } else {
            this
        }
    }

    companion object {

        @JvmStatic val EMPTY = ROI()

        @JvmStatic
        internal fun Int.contraintTo(max: Int): Int {
            return if (this < 0) 0 else if (this >= max) max - 1 else this
        }
    }
}
