package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import java.awt.Graphics2D
import java.awt.RenderingHints

abstract class Draw : TransformAlgorithm {

    abstract fun draw(source: Image, graphics: Graphics2D)

    final override fun transform(source: Image): Image {
        val graphics = source.graphics as Graphics2D
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        draw(source, graphics)
        graphics.dispose()
        return source
    }
}
