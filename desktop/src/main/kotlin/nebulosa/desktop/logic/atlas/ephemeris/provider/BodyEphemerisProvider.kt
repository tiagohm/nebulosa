package nebulosa.desktop.logic.atlas.ephemeris.provider

import nebulosa.constants.DAYSEC
import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsEphemeris
import nebulosa.horizons.HorizonsQuantity
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.VSOP87E
import nebulosa.nova.position.Barycentric
import nebulosa.nova.position.GeographicPosition
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Service
class BodyEphemerisProvider : EphemerisProvider<Body> {

    private val timeCache = ArrayList<Pair<UTC, LocalDateTime>>(1441)
    private val ephemerisCache = hashMapOf<GeographicPosition, MutableMap<Body, HorizonsEphemeris>>()

    private fun computeTime(): Boolean {
        val now = OffsetDateTime.now()
        val offset = now.offset.totalSeconds / DAYSEC
        val startTime = TimeYMDHMS(now.year, now.monthValue, now.dayOfMonth, 12) - offset

        return if (timeCache.isEmpty() || startTime.value > timeCache[0].first.value) {
            timeCache.clear()

            val stepCount = 24.0 * 60.0

            for (i in 0..stepCount.toInt()) {
                val fraction = i / stepCount // 0..1
                val utc = UTC(startTime.value, fraction)
                timeCache.add(utc to utc.asDateTime())
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
        if (!computeTime() && !force && position in ephemerisCache && target in ephemerisCache[position]!!) {
            return ephemerisCache[position]!![target]
        }

        val site = VSOP87E.EARTH + position

        val elements = Array(timeCache.size) {
            val astrometric = site.at<Barycentric>(timeCache[it].first).observe(target)
            val (az, alt) = astrometric.horizontal()
            val (ra, dec) = astrometric.equatorialAtDate()
            val (raJ2000, decJ2000) = astrometric.equatorialJ2000()

            val element = HorizonsElement(timeCache[it].second)
            element[HorizonsQuantity.ASTROMETRIC_RA] = "${raJ2000.degrees}"
            element[HorizonsQuantity.ASTROMETRIC_DEC] = "${decJ2000.degrees}"
            element[HorizonsQuantity.APPARENT_RA] = "${ra.degrees}"
            element[HorizonsQuantity.APPARENT_DEC] = "${dec.degrees}"
            element[HorizonsQuantity.APPARENT_AZ] = "${az.degrees}"
            element[HorizonsQuantity.APPARENT_ALT] = "${alt.degrees}"
            element
        }

        if (elements.isEmpty()) return null
        if (position !in ephemerisCache) ephemerisCache[position] = hashMapOf()

        return HorizonsEphemeris(elements).also { ephemerisCache[position]!![target] = it }
    }
}
