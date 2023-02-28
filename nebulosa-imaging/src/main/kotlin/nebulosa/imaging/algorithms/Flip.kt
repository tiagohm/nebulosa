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

                    val si = source.indexAt(sx, y)
                    val ei = source.indexAt(x, y)

                    for (i in 0 until source.pixelStride) {
                        val p = source.data[i][si]
                        source.data[i][si] = source.data[i][ei]
                        source.data[i][ei] = p
                    }
                }
            }
        }

        if (vertical) {
            for (y in 0 until source.height / 2) {
                val sy = source.height - y - 1

                for (x in 0 until source.width) {
                    val si = source.indexAt(x, sy)
                    val ei = source.indexAt(x, y)

                    for (i in 0 until source.pixelStride) {
                        val p = source.data[i][si]
                        source.data[i][si] = source.data[i][ei]
                        source.data[i][ei] = p
                    }
                }
            }
        }

        return source
    }
}
