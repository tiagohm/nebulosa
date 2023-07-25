package nebulosa.api.services.ephemeris

import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsQuantity
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.VSOP87E
import nebulosa.nova.position.Barycentric
import nebulosa.nova.position.GeographicPosition
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class BodyEphemerisProvider : CachedEphemerisProvider<Body>() {

    private val timeBucket = HashMap<LocalDateTime, UTC>()

    override fun compute(
        target: Body,
        position: GeographicPosition,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<HorizonsElement> {
        val site = VSOP87E.EARTH + position

        var time = startTime
        val intervalInMinutes = ChronoUnit.MINUTES.between(startTime, endTime).toInt() + 1
        val res = ArrayList<HorizonsElement>(intervalInMinutes)

        while (time <= endTime) {
            val utc = timeBucket.getOrPut(time) { UTC(TimeYMDHMS(time)) }

            val astrometric = site.at<Barycentric>(utc).observe(target)
            val (az, alt) = astrometric.horizontal()
            val (ra, dec) = astrometric.equatorialAtDate()
            val (raJ2000, decJ2000) = astrometric.equatorialJ2000()

            val element = HorizonsElement(time)
            element[HorizonsQuantity.ASTROMETRIC_RA] = "${raJ2000.normalized.degrees}"
            element[HorizonsQuantity.ASTROMETRIC_DEC] = "${decJ2000.degrees}"
            element[HorizonsQuantity.APPARENT_RA] = "${ra.normalized.degrees}"
            element[HorizonsQuantity.APPARENT_DEC] = "${dec.degrees}"
            element[HorizonsQuantity.APPARENT_AZ] = "${az.normalized.degrees}"
            element[HorizonsQuantity.APPARENT_ALT] = "${alt.degrees}"
            res.add(element)

            time = time.plusMinutes(1L)
        }

        return res
    }
}
