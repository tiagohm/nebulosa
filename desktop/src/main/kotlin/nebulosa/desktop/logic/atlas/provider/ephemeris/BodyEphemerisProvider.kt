package nebulosa.desktop.logic.atlas.provider.ephemeris

import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsEphemeris
import nebulosa.horizons.HorizonsQuantity
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.VSOP87E
import nebulosa.nova.position.Barycentric
import nebulosa.nova.position.GeographicPosition
import nebulosa.time.UTC
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BodyEphemerisProvider : AbstractEphemerisProvider<Body>() {

    override fun compute(
        target: Body,
        position: GeographicPosition,
        timeSpan: List<Pair<UTC, LocalDateTime>>,
        vararg quantities: HorizonsQuantity
    ): HorizonsEphemeris? {
        val site = VSOP87E.EARTH + position

        val elements = Array(timeSpan.size) {
            val astrometric = site.at<Barycentric>(timeSpan[it].first).observe(target)
            val (az, alt) = astrometric.horizontal()
            val (ra, dec) = astrometric.equatorialAtDate()
            val (raJ2000, decJ2000) = astrometric.equatorialJ2000()

            val element = HorizonsElement(timeSpan[it].second)
            element[HorizonsQuantity.ASTROMETRIC_RA] = "${raJ2000.degrees}"
            element[HorizonsQuantity.ASTROMETRIC_DEC] = "${decJ2000.degrees}"
            element[HorizonsQuantity.APPARENT_RA] = "${ra.degrees}"
            element[HorizonsQuantity.APPARENT_DEC] = "${dec.degrees}"
            element[HorizonsQuantity.APPARENT_AZ] = "${az.degrees}"
            element[HorizonsQuantity.APPARENT_ALT] = "${alt.degrees}"
            element
        }

        if (elements.isEmpty()) return null

        return HorizonsEphemeris(elements)
    }
}
