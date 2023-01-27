package nebulosa.desktop.gui.mount

import nebulosa.desktop.gui.View
import nebulosa.math.Angle
import java.time.LocalDateTime

interface MountView : View {

    var status: String

    val targetCoordinates: Pair<Angle, Angle>

    var isJ2000: Boolean

    fun updateTargetPosition(ra: Angle, dec: Angle)

    fun updateLSTAndMeridian(lst: Angle, timeLeftToMeridianFlip: Angle, timeToMeridianFlip: LocalDateTime)
}
