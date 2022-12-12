package nebulosa.fits.algorithms

class Blur : Convolution(KERNEL) {

    companion object {

        @JvmStatic private val KERNEL = arrayOf(
            floatArrayOf(1f, 2f, 3f, 2f, 1f),
            floatArrayOf(2f, 4f, 5f, 4f, 2f),
            floatArrayOf(3f, 5f, 6f, 5f, 3f),
            floatArrayOf(2f, 4f, 5f, 4f, 2f),
            floatArrayOf(1f, 2f, 3f, 2f, 1f),
        )
    }
}
