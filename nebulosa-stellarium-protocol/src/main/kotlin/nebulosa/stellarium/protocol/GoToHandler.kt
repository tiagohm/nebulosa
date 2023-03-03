package nebulosa.stellarium.protocol

import nebulosa.math.Angle

fun interface GoToHandler {

    fun goTo(rightAscension: Angle, declination: Angle, j2000: Boolean)
}
