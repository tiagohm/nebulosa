package nebulosa.imaging.algorithms

import nebulosa.imaging.Image

object Invert : TransformAlgorithm {

    override fun transform(source: Image): Image {
        for (i in source.data.indices) {
            source.data[i] = 1f - source.data[i]
        }

        return source
    }
}
