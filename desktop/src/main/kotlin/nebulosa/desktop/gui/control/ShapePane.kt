package nebulosa.desktop.gui.control

import javafx.application.Platform
import javafx.beans.property.DoubleProperty
import javafx.beans.property.DoublePropertyBase
import javafx.scene.Node
import javafx.scene.layout.Pane

abstract class ShapePane : Pane() {

    val widthProperty: DoubleProperty = object : DoublePropertyBase() {

        override fun getBean() = this@ShapePane

        override fun getName() = "width"
    }

    val heightProperty: DoubleProperty = object : DoublePropertyBase() {

        override fun getBean() = this@ShapePane

        override fun getName() = "height"
    }

    internal abstract fun redraw(width: Double, height: Double)

    fun redraw() {
        if (Platform.isFxApplicationThread()) redraw(widthProperty.get(), heightProperty.get())
        else Platform.runLater { redraw(widthProperty.get(), heightProperty.get()) }
    }

    final override fun isResizable() = true

    final override fun resize(width: Double, height: Double) {
        if (width != widthProperty.get() || height != heightProperty.get()) {
            widthProperty.set(width)
            heightProperty.set(height)

            redraw(width, height)
        }
    }

    protected fun add(node: Node) {
        children.add(node)
    }

    protected fun remove(node: Node) {
        children.remove(node)
    }
}
