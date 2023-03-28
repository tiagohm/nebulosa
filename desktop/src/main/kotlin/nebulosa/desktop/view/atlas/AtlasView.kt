package nebulosa.desktop.view.atlas

import eu.hansolo.fx.charts.data.XYItem
import nebulosa.desktop.view.View
import nebulosa.math.Angle
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject
import kotlin.math.min

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

    data class Star(
        val skyObject: SkyObject,
        val name: String = skyObject.names.firstOrNull() ?: "?",
        val magnitude: Double = min(skyObject.mV, skyObject.mB),
        val constellation: String = skyObject.constellation.iau,
    )

    data class DSO(
        val skyObject: SkyObject,
        val name: String = skyObject.names.firstOrNull() ?: "?",
        val magnitude: Double = min(skyObject.mV, skyObject.mB),
        val type: String = skyObject.type.description,
        val constellation: String = skyObject.constellation.iau,
    )

    fun drawAltitude(
        points: List<XYItem>, now: Double,
        civilDawn: DoubleArray, nauticalDawn: DoubleArray, astronomicalDawn: DoubleArray,
        civilDusk: DoubleArray, nauticalDusk: DoubleArray, astronomicalDusk: DoubleArray,
        night: DoubleArray,
    )

    fun updateSunImage()

    fun updateMoonImage(phase: Double, age: Double, angle: Angle)

    fun updateEquatorialCoordinates(ra: Angle, dec: Angle, raJ2000: Angle, decJ2000: Angle, constellation: Constellation?)

    fun updateHorizontalCoordinates(az: Angle, alt: Angle)

    fun clearAltitudeAndCoordinates()

    fun populatePlanet(planets: List<Planet>)

    fun populateMinorPlanet(minorPlanets: List<MinorPlanet>)

    fun populateStar(stars: List<Star>)

    fun populateDSOs(dsos: List<DSO>)

    fun updateInfo(bodyName: String)

    fun updateRTS(rts: Triple<String, String, String>)
}
