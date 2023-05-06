package nebulosa.skycatalog

import nebulosa.math.Angle

interface AxisSize {

    val minorAxis: Angle

    val majorAxis: Angle

    val orientation: Angle
}
