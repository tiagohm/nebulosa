package nebulosa.imaging.algorithms.transformation

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.TransformAlgorithm

object Invert : TransformAlgorithm {

    override fun transform(source: Image): Image {
        for (i in source.r.indices) source.r[i] = 1f - source.r[i]

        if (!source.mono) {
            for (i in source.g.indices) source.g[i] = 1f - source.g[i]
            for (i in source.b.indices) source.b[i] = 1f - source.b[i]
        }

        return source
    }
}
