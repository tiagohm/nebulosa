package nebulosa.desktop.gui.control

import javafx.beans.property.DoubleProperty
import javafx.beans.property.DoublePropertyBase
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.shape.Line

// TODO: Como eliminar o comportamento do Group?
// O Group ajusta seu tamanho com base na maior distancia entre
// seus "Shapes", tanto no eixo x quanto no eixo y.
// Ex: se há um Shape com x = -100 e outro com x = 100, o Group
// terá pelo menos largura = 200. Então, em vez do shape com x = -100
// ficar do lado de fora, ele é desenhado dentro, como se fosse x = 0.
// Pelo menos foi isso que identifiquei e se fez necessário a gambiarra abaixo,
// além de não permitir adicionar qualquer Shape que ultrapasse os limites da imagem.
abstract class Drawable : Group() {

    // Workaround to fit to the parent.
    private val fakeLineHor = Line()
    private val fakeLineVer = Line()

    val widthProperty: DoubleProperty = object : DoublePropertyBase() {

        override fun getBean() = this@Drawable

        override fun getName() = "width"
    }

    val heightProperty: DoubleProperty = object : DoublePropertyBase() {

        override fun getBean() = this@Drawable

        override fun getName() = "height"
    }

    init {
        add(fakeLineHor)
        add(fakeLineVer)

        fakeLineHor.startX = 0.0
        fakeLineHor.startY = 0.0
        fakeLineHor.endX = 0.0

        fakeLineVer.startX = 0.0
        fakeLineVer.startY = 0.0
        fakeLineVer.endY = 0.0
    }

    internal abstract fun redraw(width: Double, height: Double)

    fun redraw() = redraw(widthProperty.get(), heightProperty.get())

    final override fun isResizable() = true

    final override fun maxHeight(width: Double) = Double.POSITIVE_INFINITY

    final override fun maxWidth(height: Double) = Double.POSITIVE_INFINITY

    final override fun minWidth(height: Double) = widthProperty.get()

    final override fun minHeight(width: Double) = heightProperty.get()

    final override fun prefWidth(height: Double) = widthProperty.get()

    final override fun prefHeight(width: Double) = heightProperty.get()

    final override fun resize(width: Double, height: Double) {
        if (width != widthProperty.get() || height != heightProperty.get()) {
            widthProperty.set(width)
            heightProperty.set(height)

            fakeLineHor.endY = height
            fakeLineVer.endX = width

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
