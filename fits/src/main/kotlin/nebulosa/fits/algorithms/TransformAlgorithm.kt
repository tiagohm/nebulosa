package nebulosa.fits.algorithms

import nebulosa.fits.FitsImage

fun interface TransformAlgorithm {

    fun transform(image: FitsImage): FitsImage

    companion object {

        @JvmStatic
        fun of(vararg algoritms: TransformAlgorithm) = TransformAlgorithm {
            algoritms.fold(it) { image, algorithm -> algorithm.transform(image) }
        }
    }
}
