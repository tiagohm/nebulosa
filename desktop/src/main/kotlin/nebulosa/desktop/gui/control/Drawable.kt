package nebulosa.desktop.gui.control

import javafx.scene.Group
import javafx.scene.Node

abstract class Drawable : Group() {

    @Volatile var width = 0.0
        private set

    @Volatile var height = 0.0
        private set

    override fun isResizable() = true

    override fun maxHeight(width: Double) = Double.POSITIVE_INFINITY

    override fun maxWidth(height: Double) = Double.POSITIVE_INFINITY

    override fun minWidth(height: Double) = 1.0

    override fun minHeight(width: Double) = 1.0

    override fun resize(width: Double, height: Double) {
        this.width = width
        this.height = height
        redraw()
    }

    abstract fun redraw()

    protected fun add(node: Node) {
        children.add(node)
    }

    protected fun remove(node: Node) {
        children.remove(node)
    }
}
