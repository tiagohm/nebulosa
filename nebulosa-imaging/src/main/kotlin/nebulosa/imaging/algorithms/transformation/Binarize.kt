package nebulosa.imaging.algorithms.transformation

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.TransformAlgorithm

data class Binarize(private val threshold: Float = 0.5f) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        for (i in 0 until source.size) {
            val pixel = source.readGray(i)
            source.writeGray(i, if (pixel >= threshold) 1f else 0f)
        }

        return source
    }
}
