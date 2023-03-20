package nebulosa.desktop.logic.image

import javafx.scene.canvas.GraphicsContext
import nebulosa.desktop.view.image.Drawable
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.PairOfAngle
import nebulosa.platesolving.Calibration
import nebulosa.skycatalog.brightstars.BrightStars
import nebulosa.stellarium.skycatalog.Nebula
import kotlin.math.acos

data class Annotation(
    val calibration: Calibration,
    val nebula: Nebula,
) : Drawable {

    private val brightStars = BrightStarsAnnotation(calibration)
    private val nebulaStars = NebulaAnnotation(calibration, nebula)

    override fun draw(width: Double, height: Double, graphics: GraphicsContext) {
        brightStars.draw(width, height, graphics)
        nebulaStars.draw(width, height, graphics)
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
