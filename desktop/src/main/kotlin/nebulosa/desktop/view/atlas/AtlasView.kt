package nebulosa.desktop.view.atlas

import eu.hansolo.fx.charts.data.XYItem
import nebulosa.desktop.view.View
import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject
import java.awt.image.BufferedImage
import java.time.LocalDate
import java.time.LocalTime

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

    val latitude: Angle

    val longitude: Angle

    val elevation: Distance

    val date: LocalDate

    val time: LocalTime

    val manualMode: Boolean

    suspend fun drawPoints(points: List<XYItem>)

    suspend fun drawNow()

    suspend fun drawTwilight(
        civilDawn: DoubleArray, nauticalDawn: DoubleArray, astronomicalDawn: DoubleArray,
        civilDusk: DoubleArray, nauticalDusk: DoubleArray, astronomicalDusk: DoubleArray,
        night: DoubleArray,
    )

    suspend fun updateSunImage(image: BufferedImage)

    suspend fun updateMoonImage(phase: Double, age: Double, angle: Angle)

    suspend fun updateEquatorialCoordinates(ra: Angle, dec: Angle, raJ2000: Angle, decJ2000: Angle, constellation: Constellation?)

    suspend fun updateHorizontalCoordinates(az: Angle, alt: Angle)

    suspend fun clearAltitudeAndCoordinates()

    suspend fun populatePlanet(planets: List<Planet>)

    suspend fun populateMinorPlanet(minorPlanets: List<MinorPlanet>)

    suspend fun populateStar(stars: List<SkyObject>)

    suspend fun populateDSOs(dsos: List<SkyObject>)

    suspend fun updateInfo(bodyName: String, extra: List<Pair<String, String>> = emptyList())

    suspend fun updateRTS(rts: Triple<String, String, String>)

    fun loadCoordinates(useCoordinatesFromMount: Boolean, latitude: Angle, longitude: Angle, elevation: Distance)
}
