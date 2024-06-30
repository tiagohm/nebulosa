package nebulosa.nova.position

import nebulosa.math.Angle
import nebulosa.math.Distance

interface GeographicCoordinate {

    val longitude: Angle

    val latitude: Angle

    val elevation: Distance
}
