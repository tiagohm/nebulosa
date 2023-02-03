package nebulosa.desktop.gui.control

import javafx.scene.canvas.Canvas

open class ResizableCanvas : Canvas() {

    override fun isResizable() = true

    override fun maxHeight(width: Double) = Double.POSITIVE_INFINITY

    override fun maxWidth(height: Double) = Double.POSITIVE_INFINITY

    override fun minWidth(height: Double) = 1.0

    override fun minHeight(width: Double) = 1.0

    override fun prefWidth(height: Double) = width

    override fun prefHeight(width: Double) = height

    override fun resize(width: Double, height: Double) {
        this.width = width
        this.height = height
    }
}
