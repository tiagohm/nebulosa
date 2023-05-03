package nebulosa.desktop.view.atlas

import nebulosa.desktop.view.View
import nebulosa.math.Angle
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType

interface SkyObjectFilterView : View {

    val filtered: Boolean

    val rightAscension: Angle

    val declination: Angle

    val radius: Angle

    val constellation: Constellation?

    val type: SkyObjectType?

    val mangitudeMin: Double

    val magnitudeMax: Double
}
