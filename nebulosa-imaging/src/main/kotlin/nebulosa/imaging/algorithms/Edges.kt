package nebulosa.imaging.algorithms

class Edges : Convolution(KERNEL) {

    companion object {

        @JvmStatic private val KERNEL = arrayOf(
            floatArrayOf(0f, -1f, 0f),
            floatArrayOf(-1f, 4f, -1f),
            floatArrayOf(0f, -1f, 0f),
        )
    }
}
