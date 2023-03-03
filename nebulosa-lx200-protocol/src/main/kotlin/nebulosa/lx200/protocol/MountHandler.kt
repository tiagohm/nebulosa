package nebulosa.lx200.protocol

import nebulosa.math.Angle

interface MountHandler {

    val rightAscension: Angle

    val declination: Angle

    val latitude: Angle

    val longitude: Angle

    val slewing: Boolean

    val tracking: Boolean

    fun goTo(rightAscension: Angle, declination: Angle)

    fun syncTo(rightAscension: Angle, declination: Angle)

    fun abort()

    // TODO: Sync coordinates, date & time.
    // TODO: Support move N/S/W/E
}
