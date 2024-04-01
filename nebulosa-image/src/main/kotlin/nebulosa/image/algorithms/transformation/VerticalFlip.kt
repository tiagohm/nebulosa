package nebulosa.image.algorithms.transformation

import nebulosa.image.Image
import nebulosa.image.algorithms.TransformAlgorithm

data object VerticalFlip : TransformAlgorithm {

    override fun transform(source: Image): Image {
        for (y in 0 until source.height / 2) {
            val sy = source.height - y - 1

            for (x in 0 until source.width) {
                val si = source.indexAt(x, sy)
                val ei = source.indexAt(x, y)

                for (i in 0 until source.numberOfChannels) {
                    val p = source.data[i][si]
                    source.data[i][si] = source.data[i][ei]
                    source.data[i][ei] = p
                }
            }
        }

        return source
    }
}
