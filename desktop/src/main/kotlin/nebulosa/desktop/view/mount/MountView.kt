package nebulosa.desktop.view.mount

import nebulosa.desktop.view.View
import nebulosa.erfa.PairOfAngle
import nebulosa.math.Angle
import java.time.LocalDateTime

interface MountView : View {

    var status: String

    val targetCoordinates: PairOfAngle

    var isJ2000: Boolean

    fun updateTargetPosition(ra: Angle, dec: Angle)

    fun updateLSTAndMeridian(lst: Angle, timeLeftToMeridianFlip: Angle, timeToMeridianFlip: LocalDateTime)
}
