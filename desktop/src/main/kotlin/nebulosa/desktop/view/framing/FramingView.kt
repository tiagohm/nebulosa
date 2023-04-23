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

    val alwaysOpenInNewWindow: Boolean

    suspend fun load(
        ra: Angle, dec: Angle,
        hips: HipsSurvey? = null,
        width: Int = frameWidth, height: Int = frameHeight,
        rotation: Angle = frameRotation,
        fov: Angle = frameFOV,
    )

    fun populateHipsSurveys(data: List<HipsSurvey>, selected: HipsSurvey?)

    suspend fun updateCoordinate(ra: Angle, dec: Angle)

    fun updateFOV(fov: Angle)
}
