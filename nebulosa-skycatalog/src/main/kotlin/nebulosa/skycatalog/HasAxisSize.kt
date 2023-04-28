package nebulosa.skycatalog

import nebulosa.math.Angle

interface HasAxisSize {

    val minorAxis: Angle

    val majorAxis: Angle

    val orientation: Angle
}
