package nebulosa.lx200.protocol

import nebulosa.math.Angle
import java.time.OffsetDateTime

interface LX200MountHandler {

    val rightAscension: Angle

    val declination: Angle

    val latitude: Angle

    val longitude: Angle

    val slewing: Boolean

    val tracking: Boolean

    val parked: Boolean

    fun goTo(rightAscension: Angle, declination: Angle)

    fun syncTo(rightAscension: Angle, declination: Angle)

    fun abort()

    fun moveNorth(enabled: Boolean)

    fun moveSouth(enabled: Boolean)

    fun moveWest(enabled: Boolean)

    fun moveEast(enabled: Boolean)

    fun time(time: OffsetDateTime)

    fun coordinates(longitude: Angle, latitude: Angle)
}
