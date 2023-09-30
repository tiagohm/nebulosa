package nebulosa.skycatalog

import nebulosa.math.Angle

interface OrientedSkyObject {

    val majorAxis: Angle

    val minorAxis: Angle

    val orientation: Angle
}
