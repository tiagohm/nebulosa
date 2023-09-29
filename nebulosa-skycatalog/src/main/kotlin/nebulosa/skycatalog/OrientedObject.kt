package nebulosa.skycatalog

import nebulosa.math.Angle

interface OrientedObject {

    val majorAxis: Angle

    val minorAxis: Angle

    val orientation: Angle
}
