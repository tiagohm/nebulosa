package nebulosa.fits.algorithms

import nebulosa.fits.FitsImage

class Flip(
    val horizontal: Boolean = false,
    val vertical: Boolean = false,
) : TransformAlgorithm {

    override fun transform(image: FitsImage): FitsImage {
        val pixelStride = if (image.isMono) 1 else 3

        if (horizontal) {
            for (y in 0 until image.height) {
                for (x in 0 until image.width / 2) {
                    val sx = image.width - x - 1

                    val si = y * image.width * pixelStride + sx * pixelStride
                    val ei = y * image.width * pixelStride + x * pixelStride

                    for (i in 0 until pixelStride) {
                        val p = image.data[si + i]
                        image.data[si + i] = image.data[ei + i]
                        image.data[ei + i] = p
                    }
                }
            }
        }

        if (vertical) {
            for (y in 0 until image.height / 2) {
                val sy = image.height - y - 1

                for (x in 0 until image.width) {
                    val si = sy * image.width * pixelStride + x * pixelStride
                    val ei = y * image.width * pixelStride + x * pixelStride

                    for (i in 0 until pixelStride) {
                        val p = image.data[si + i]
                        image.data[si + i] = image.data[ei + i]
                        image.data[ei + i] = p
                    }
                }
            }
        }

        return image
    }
}
