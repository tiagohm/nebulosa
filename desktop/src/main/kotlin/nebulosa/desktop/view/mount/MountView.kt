package nebulosa.desktop.view.mount

import nebulosa.desktop.view.View
import nebulosa.math.Angle
import nebulosa.nova.astrometry.Constellation
import java.time.LocalDateTime

interface MountView : View {

    var status: String

    val targetRightAscension: Angle

    val targetDeclination: Angle

    var isJ2000: Boolean

    suspend fun updateTargetPosition(ra: Angle, dec: Angle)

    suspend fun updateLSTAndMeridian(lst: Angle, timeLeftToMeridianFlip: Angle, timeToMeridianFlip: LocalDateTime)

    suspend fun updateTargetInfo(
        azimuth: Angle, altitude: Angle, constellation: Constellation,
        timeLeftToMeridianFlip: Angle, timeToMeridianFlip: LocalDateTime,
    )
}
