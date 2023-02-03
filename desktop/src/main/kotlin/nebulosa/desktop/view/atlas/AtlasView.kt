package nebulosa.desktop.view.atlas

import javafx.geometry.Point2D
import nebulosa.desktop.view.View

interface AtlasView : View {

    enum class TabType {
        SUN,
        MOON,
        PLANET,
        MINOR_PLANET,
        STAR,
        DSO,
    }

    fun drawAltitudeGraph(
        points: List<Point2D>, now: Double,
        civilTwilight: Twilight, nauticalTwilight: Twilight, astronomicalTwilight: Twilight,
    )

    fun updateSunImage(path: String)
}
