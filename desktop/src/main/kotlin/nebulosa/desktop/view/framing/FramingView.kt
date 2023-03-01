package nebulosa.desktop.view.framing

import nebulosa.desktop.view.View
import nebulosa.hips2fits.HipsSurvey
import nebulosa.math.Angle

interface FramingView : View {

    val hipsSurvey: HipsSurvey?

    val frameRA: Angle

    val frameDEC: Angle

    val frameWidth: Int

    val frameHeight: Int

    val frameFOV: Angle

    val frameRotation: Angle

    fun load(
        ra: Angle, dec: Angle,
        hips: HipsSurvey? = null,
        width: Int = 1200, height: Int = 900,
        rotation: Angle = Angle.ZERO,
        fov: Angle? = null,
    )

    fun populateHipsSurveys(data: List<HipsSurvey>, selected: HipsSurvey?)

    fun updateCoordinate(ra: Angle, dec: Angle)

    fun updateFOV(fov: Angle)
}
