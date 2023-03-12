package nebulosa.desktop.gui.control

import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.transform.Rotate
import nebulosa.math.Angle
import kotlin.math.min

class MoonView : Canvas() {

    private val images = HashMap<Int, Image>(30)

    fun draw(age: Double, angle: Angle) {
        val gc = graphicsContext2D

        val centerX = width / 2
        val centerY = height / 2

        val size = min(width, height)

        val r = Rotate(angle.degrees, centerX, centerY)
        gc.setTransform(r.mxx, r.myx, r.mxy, r.myy, r.tx, r.ty)

        val phaseNum = ((age * 1.01589576574604) % 30.0).toInt() + 1
        val image = images.getOrPut(phaseNum) { Image("images/moon/phases/%02d.png".format(phaseNum)) }
        gc.drawImage(image, centerX - size / 2, centerY - size / 2, size, size)
    }
}
