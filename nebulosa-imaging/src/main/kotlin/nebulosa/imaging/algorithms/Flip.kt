package nebulosa.imaging.algorithms

import nebulosa.imaging.Image

class Flip(
    val horizontal: Boolean = false,
    val vertical: Boolean = false,
) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        if (horizontal) {
            for (y in 0 until source.height) {
                for (x in 0 until source.width / 2) {
                    val sx = source.width - x - 1

                    val si = y * source.width * source.pixelStride + sx * source.pixelStride
                    val ei = y * source.width * source.pixelStride + x * source.pixelStride

                    for (i in 0 until source.pixelStride) {
                        val p = source.data[si + i]
                        source.data[si + i] = source.data[ei + i]
                        source.data[ei + i] = p
                    }
                }
            }
        }

        if (vertical) {
            for (y in 0 until source.height / 2) {
                val sy = source.height - y - 1

                for (x in 0 until source.width) {
                    val si = sy * source.width * source.pixelStride + x * source.pixelStride
                    val ei = y * source.width * source.pixelStride + x * source.pixelStride

                    for (i in 0 until source.pixelStride) {
                        val p = source.data[si + i]
                        source.data[si + i] = source.data[ei + i]
                        source.data[ei + i] = p
                    }
                }
            }
        }

        return source
    }
}
