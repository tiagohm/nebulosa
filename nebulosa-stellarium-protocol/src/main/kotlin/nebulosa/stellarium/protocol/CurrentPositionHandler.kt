package nebulosa.stellarium.protocol

import nebulosa.math.Angle

fun interface CurrentPositionHandler {

    fun sendCurrentPosition(rightAscension: Angle, declination: Angle)
}
