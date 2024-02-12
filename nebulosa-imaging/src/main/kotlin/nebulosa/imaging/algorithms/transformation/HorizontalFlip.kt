package nebulosa.imaging.algorithms.transformation

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.TransformAlgorithm

data object HorizontalFlip : TransformAlgorithm {

    override fun transform(source: Image): Image {
        for (y in 0 until source.height) {
            for (x in 0 until source.width / 2) {
                val sx = source.width - x - 1

                val si = source.indexAt(sx, y)
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
