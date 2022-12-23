package nebulosa.imaging.algorithms

class Mean : Convolution(KERNEL) {

    companion object {

        @JvmStatic private val KERNEL = arrayOf(
            floatArrayOf(1f, 1f, 1f),
            floatArrayOf(1f, 1f, 1f),
            floatArrayOf(1f, 1f, 1f),
        )
    }
}
