package nebulosa.desktop.logic.atlas

import nebulosa.constants.DAYSEC
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.VSOP87E
import nebulosa.nova.position.Barycentric
import nebulosa.nova.position.GeographicPosition
import nebulosa.query.horizons.HorizonsElement
import nebulosa.query.horizons.HorizonsEphemeris
import nebulosa.query.horizons.HorizonsQuantity
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Service
class BodyEphemerisProvider : EphemerisProvider<Body> {

    private val time = ArrayList<Pair<UTC, LocalDateTime>>(1441)
    private val cache = hashMapOf<GeographicPosition, MutableMap<Body, HorizonsEphemeris>>()

    private fun computeTime(): Boolean {
        val now = OffsetDateTime.now()
        val offset = now.offset.totalSeconds / DAYSEC
        val startTime = TimeYMDHMS(now.year, now.monthValue, now.dayOfMonth, 12) - offset

        return if (time.isEmpty() || startTime.value > time[0].first.value) {
            time.clear()

            val stepCount = 24.0 * 60.0

            for (i in 0..stepCount.toInt()) {
                val fraction = i / stepCount // 0..1
                val utc = UTC(startTime.value, fraction)
                time.add(utc to utc.asDateTime())
            }

            true
        } else {
            false
        }
    }

    override fun compute(
        target: Body,
        position: GeographicPosition,
        force: Boolean,
    ): HorizonsEphemeris? {
        if (!computeTime() && !force && position in cache && target in cache[position]!!) {
            return cache[position]!![target]
        }

        val site = VSOP87E.EARTH + position

        val ephemeris = HashMap<LocalDateTime, HorizonsElement>(time.size)

        time.forEach {
            val astrometric = site.at<Barycentric>(it.first).observe(target)
            val (az, alt) = astrometric.horizontal()
            val (ra, dec) = astrometric.equatorialAtDate()
            val (raJ2000, decJ2000) = astrometric.equatorialJ2000()

            val element = HorizonsElement()
            element[HorizonsQuantity.ASTROMETRIC_RA] = "${raJ2000.degrees}"
            element[HorizonsQuantity.ASTROMETRIC_DEC] = "${decJ2000.degrees}"
            element[HorizonsQuantity.APPARENT_RA] = "${ra.degrees}"
            element[HorizonsQuantity.APPARENT_DEC] = "${dec.degrees}"
            element[HorizonsQuantity.APPARENT_AZ] = "${az.degrees}"
            element[HorizonsQuantity.APPARENT_ALT] = "${alt.degrees}"
            ephemeris[it.second] = element
        }

        if (position !in cache) cache[position] = hashMapOf()

        return HorizonsEphemeris.of(ephemeris).also { cache[position]!![target] = it }
    }
}
