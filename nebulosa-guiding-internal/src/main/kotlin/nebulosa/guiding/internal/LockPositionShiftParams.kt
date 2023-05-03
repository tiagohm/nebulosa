package nebulosa.guiding.internal

internal data class LockPositionShiftParams(
    var shiftEnabled: Boolean = false,
    val shiftRate: Point = Point(),
    var shiftIsMountCoords: Boolean = false,
    val shiftUnit: ShiftUnit = ShiftUnit.PIXEL,
)
