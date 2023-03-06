package nebulosa.desktop.gui.image

import javafx.scene.canvas.GraphicsContext
import nebulosa.desktop.view.image.Drawable
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.PairOfAngle
import nebulosa.platesolving.Calibration
import kotlin.math.acos

data class Annotation(val calibration: Calibration) : Drawable {

    private val namedStars = NamedStarsAnnotation(calibration)
    private val messier = MessierAnnotation(calibration)
    private val ngc = NGCAnnotation(calibration)

    override fun draw(width: Double, height: Double, graphics: GraphicsContext) {
        namedStars.draw(width, height, graphics)
        messier.draw(width, height, graphics)
        ngc.draw(width, height, graphics)
    }

    companion object {

        @JvmStatic
        internal fun distanceBetween(a: PairOfAngle, b: PairOfAngle): Angle {
            // cos(d) = sin(δ₁)sin(δ₂) + cos(δ₁)cos(δ₂)cos(α₁-α₂)
            val cosD = a.second.sin * b.second.sin + a.second.cos * b.second.cos * (a.first - b.first).cos
            return acos(cosD).rad
        }
    }
}
