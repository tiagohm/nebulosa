package nebulosa.imaging.algorithms

interface ConvolutionKernel {

    val xSize: Int

    val ySize: Int

    operator fun get(index: Int): Float

    operator fun get(x: Int, y: Int) = get(y * xSize + x)

    val divisor: Float
}
