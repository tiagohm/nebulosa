package nebulosa.desktop.view.atlas

import javafx.geometry.Point2D
import nebulosa.desktop.view.View
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.mas
import nebulosa.math.Velocity.Companion.kms
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.astrometry.FixedStar
import nebulosa.simbad.SimbadObject

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
        val simbad: SimbadObject,
        val name: String = simbad.name,
        val magnitude: Double = simbad.v,
        val type: String = simbad.type.description,
    ) {

        val star by lazy { FixedStar(simbad.ra.deg, simbad.dec.deg, simbad.pmRA.mas, simbad.pmDEC.mas, simbad.plx.mas, simbad.rv.kms) }
    }

    data class DSO(
        val simbad: SimbadObject,
        val name: String = simbad.name,
        val magnitude: Double = simbad.v,
        val type: String = simbad.type.description,
    ) {

        val star by lazy { FixedStar(simbad.ra.deg, simbad.dec.deg, simbad.pmRA.mas, simbad.pmDEC.mas, simbad.plx.mas, simbad.rv.kms) }
    }

    fun updateAltitude(
        points: List<Point2D>, now: Double,
        civilTwilight: Twilight, nauticalTwilight: Twilight, astronomicalTwilight: Twilight,
    )

    fun updateSunImage(uri: String)

    fun updateMoonImage(uri: String)

    fun updateEquatorialCoordinates(ra: Angle, dec: Angle, raJ2000: Angle, decJ2000: Angle, constellation: Constellation?)

    fun updateHorizontalCoordinates(az: Angle, alt: Angle)

    fun clearAltitudeAndCoordinates()

    fun populatePlanet(planets: List<Planet>)

    fun populateMinorPlanet(minorPlanets: List<MinorPlanet>)

    fun populateStar(stars: List<Star>)

    fun populateDSO(dso: List<DSO>)

    fun updateInfo(bodyName: String)
}
