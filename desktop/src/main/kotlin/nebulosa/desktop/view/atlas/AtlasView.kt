package nebulosa.desktop.view.atlas

import javafx.geometry.Point2D
import nebulosa.desktop.view.View
import nebulosa.math.Angle

interface AtlasView : View {

    enum class TabType {
        SUN,
        MOON,
        PLANET,
        MINOR_PLANET,
        STAR,
        DSO,
    }

    data class Planet(
        val name: String,
        val type: String,
        val command: String,
    )

    data class MinorPlanet(
        val element: String,
        val description: String,
        val value: String,
    )

    fun drawAltitude(
        points: List<Point2D>, now: Double,
        civilTwilight: Twilight, nauticalTwilight: Twilight, astronomicalTwilight: Twilight,
    )

    fun updateSunImage(uri: String)

    fun updateMoonImage(uri: String)

    fun updateEquatorialCoordinates(ra: Angle, dec: Angle, raJ2000: Angle, decJ2000: Angle)

    fun updateHorizontalCoordinates(az: Angle, alt: Angle)

    fun clearAltitudeAndCoordinates()

    fun populatePlanets(planets: List<Planet>)

    fun populateMinorPlanet(minorPlanet: List<MinorPlanet>)
}
