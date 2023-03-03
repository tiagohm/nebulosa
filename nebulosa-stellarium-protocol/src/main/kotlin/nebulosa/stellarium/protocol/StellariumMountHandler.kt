package nebulosa.stellarium.protocol

import nebulosa.math.Angle

interface StellariumMountHandler {

    val rightAscension: Angle

    val declination: Angle

    val rightAscensionJ2000: Angle

    val declinationJ2000: Angle

    fun goTo(rightAscension: Angle, declination: Angle, j2000: Boolean = false)
}
