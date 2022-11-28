package nebulosa.erfa

import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.m

enum class EllipsoidType(val radius: Distance, val flattening: Double) {
    WGS84(6378137.0.m, 1.0 / 298.257223563),
    GRS80(6378137.0.m, 1.0 / 298.257222101),
    WGS72(6378135.0.m, 1.0 / 298.26),
    IERS2010(6378136.6.m, 1.0 / 298.25642),
}
