package nebulosa.imaging.algorithms.transformation

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.TransformAlgorithm
import java.util.*

data class SaltAndPepperNoise(
    private val amount: Float,
    private val random: Random = Random(),
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
            val index = source.indexAt(x, y)
            val plane = if (source.mono) 0 else random.nextInt(3)
            source.data[plane][index] = if (random.nextBoolean()) 1f else 0f
        }

        return source
    }
}
