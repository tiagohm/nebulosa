package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import java.util.*

class SaltAndPepperNoise(
    val amount: Float,
    val random: Random = Random(),
) : TransformAlgorithm {

    init {
        require(amount <= 1f) { "amount <= 1: $amount" }
        require(amount >= 0f) { "amount >= 0: $amount" }
    }

    override fun transform(source: Image): Image {
        val noisyPixels = (source.width * source.height * amount).toInt()

        for (i in 0 until noisyPixels) {
            val x = random.nextInt(source.width)
            val y = random.nextInt(source.height)
            val index = y * source.stride + x * source.pixelStride
            val plane = if (source.mono) 0 else random.nextInt(3)
            source.data[index + plane] = if (random.nextBoolean()) 1f else 0f
        }

        return source
    }
}
