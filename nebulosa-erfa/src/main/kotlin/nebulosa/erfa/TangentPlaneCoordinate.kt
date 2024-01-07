package nebulosa.erfa

import nebulosa.math.Angle

data class TangentPlaneCoordinate(
    @JvmField val xi: Angle,
    @JvmField val eta: Angle,
    @JvmField val j: Int,
)
