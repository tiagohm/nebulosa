package nebulosa.fits.algorithms

import nebulosa.fits.Image

fun interface PartialTransformAlgorithm : TransformAlgorithm {

    fun transform(source: Image, destination: Image)

    override fun transform(source: Image): Image {
        val destination = source.clone()
        transform(source, destination)
        destination.data.copyInto(source.data)
        return source
    }
}
