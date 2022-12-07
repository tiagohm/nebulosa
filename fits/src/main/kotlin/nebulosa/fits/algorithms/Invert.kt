package nebulosa.fits.algorithms

import nebulosa.fits.FitsImage

class Invert(val enable: Boolean = true) : TransformAlgorithm {

    override fun transform(image: FitsImage): FitsImage {
        if (enable) {
            for (i in image.data.indices) {
                image.data[i] = 1f - image.data[i]
            }
        }

        return image
    }
}
