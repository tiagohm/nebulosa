package nebulosa.guiding.local

data class LockPositionShiftParams(
    var shiftEnabled: Boolean = false,
    var shiftRate: Point = Point.ZERO,
    var shiftIsMountCoords: Boolean = false,
)
