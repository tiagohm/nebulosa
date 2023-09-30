package nebulosa.skycatalog

import nebulosa.math.Angle

interface OrientedSkyObject : SkyObject {

    val majorAxis: Angle

    val minorAxis: Angle

    val orientation: Angle
}
