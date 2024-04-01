package nebulosa.image.algorithms.transformation

import nebulosa.image.Image
import nebulosa.image.algorithms.TransformAlgorithm

data object Invert : TransformAlgorithm {

    override fun transform(source: Image): Image {
        for (i in source.red.indices) source.red[i] = 1f - source.red[i]

        if (!source.mono) {
            for (i in source.green.indices) source.green[i] = 1f - source.green[i]
            for (i in source.blue.indices) source.blue[i] = 1f - source.blue[i]
        }

        return source
    }
}
