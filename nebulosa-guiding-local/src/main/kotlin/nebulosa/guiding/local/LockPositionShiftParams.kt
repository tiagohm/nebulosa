package nebulosa.guiding.local

data class LockPositionShiftParams(
    var shiftEnabled: Boolean = false,
    var shiftRate: Point = Point(),
    var shiftIsMountCoords: Boolean = false,
    val shiftUnit: ShiftUnit = ShiftUnit.PIXEL
)
