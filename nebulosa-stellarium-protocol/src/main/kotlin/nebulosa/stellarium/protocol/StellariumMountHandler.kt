package nebulosa.stellarium.protocol

import nebulosa.math.Angle

interface StellariumMountHandler {

    val rightAscension: Angle

    val declination: Angle

    fun goTo(rightAscension: Angle, declination: Angle)
}
