package nebulosa.desktop.view.image

import javafx.scene.canvas.GraphicsContext

interface Drawable {

    fun draw(width: Double, height: Double, graphics: GraphicsContext)
}
