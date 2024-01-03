package nebulosa.erfa

import nebulosa.math.Angle

data class EulerAngles(
    @JvmField val zeta: Angle,
    @JvmField val z: Angle,
    @JvmField val theta: Angle,
)
