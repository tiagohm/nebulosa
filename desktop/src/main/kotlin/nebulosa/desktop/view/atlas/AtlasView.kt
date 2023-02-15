package nebulosa.desktop.view.atlas

import javafx.geometry.Point2D
import nebulosa.desktop.view.View
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.mas
import nebulosa.math.Velocity.Companion.kms
import nebulosa.nova.astrometry.FixedStar
import nebulosa.query.simbad.SimbadObject

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
        val star: FixedStar = FixedStar(simbad.ra.deg, simbad.dec.deg, simbad.pmRA.mas, simbad.pmDEC.mas, simbad.plx.mas, simbad.rv.kms),
        val name: String = simbad.name,
        val pmRA: String = if (simbad.pmRA.isFinite()) "${simbad.pmRA}" else "-",
        val pmDEC: String = if (simbad.pmDEC.isFinite()) "${simbad.pmDEC}" else "-",
        val plx: String = if (simbad.plx.isFinite()) "${simbad.plx}" else "-",
        val rv: String = if (simbad.rv.isFinite()) "${simbad.rv}" else "-",
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

    fun populateMinorPlanets(minorPlanets: List<MinorPlanet>)

    fun populateStars(stars: List<Star>)
}
