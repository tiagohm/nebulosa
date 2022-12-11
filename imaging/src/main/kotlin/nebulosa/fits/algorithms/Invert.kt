package nebulosa.fits.algorithms

import nebulosa.fits.Image

object Invert : TransformAlgorithm {

    override fun transform(source: Image): Image {
        for (i in source.data.indices) {
            source.data[i] = 1f - source.data[i]
        }

        return source
    }
}
