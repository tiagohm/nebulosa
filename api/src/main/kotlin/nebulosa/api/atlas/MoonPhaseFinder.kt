package nebulosa.api.atlas

import nebulosa.constants.PIOVERTWO
import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsQuantity
import nebulosa.horizons.HorizonsService
import nebulosa.horizons.ObservingSite
import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.deg
import nebulosa.nova.almanac.computeDiffAndReduceToIndices
import nebulosa.nova.position.GeographicCoordinate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import kotlin.math.floor

class MoonPhaseFinder(private val horizonsService: HorizonsService) {

    fun find(
        date: LocalDate,
        longitude: Angle, latitude: Angle, elevation: Distance = 0.0,
        offsetInMinutes: Long = 0L,
    ) = find(date, ObservingSite.Geographic(longitude, latitude, elevation), offsetInMinutes)

    fun find(date: LocalDate, location: GeographicCoordinate, offsetInMinutes: Long = 0L): List<MoonPhaseDateTime> {
        return find(date, ObservingSite.Geographic(location.longitude, location.latitude, location.elevation), offsetInMinutes)
    }

    fun find(date: LocalDate, offsetInMinutes: Long = 0L): List<MoonPhaseDateTime> {
        return find(date, ObservingSite.Geocentric.EARTH, offsetInMinutes)
    }

    private fun find(date: LocalDate, site: ObservingSite, offsetInMinutes: Long = 0L): List<MoonPhaseDateTime> {
        val startTime = LocalDateTime.of(date.withDayOfMonth(1), LocalTime.MIN).minusMinutes(offsetInMinutes)
        val endTime = LocalDateTime.of(date.with(TemporalAdjusters.lastDayOfMonth()), LocalTime.MAX).minusMinutes(offsetInMinutes)
        return compute(60, startTime, endTime, site, offsetInMinutes)
    }

    private fun compute(
        stepSizeInMinutes: Int,
        startTime: LocalDateTime, endTime: LocalDateTime,
        site: ObservingSite, offsetInMinutes: Long,
    ): List<MoonPhaseDateTime> {
        val sun = horizonsService.observer(
            "10", site,
            startTime, endTime,
            stepSizeInMinutes,
            extraPrecision = false,
            quantities = QUANTITIES,
        ).execute().body()!!

        val moon = horizonsService.observer(
            "301", site,
            startTime, endTime,
            stepSizeInMinutes,
            extraPrecision = false,
            quantities = QUANTITIES,
        ).execute().body()!!

        val phases = IntArray(sun.size) { floor((moon[it].eclipticLongitude() - sun[it].eclipticLongitude()) / PIOVERTWO).mod(4.0).toInt() }
        val indices = phases.computeDiffAndReduceToIndices()

        if (stepSizeInMinutes == 1) {
            return indices.map { MoonPhaseDateTime(moon[it].dateTime.plusMinutes(offsetInMinutes), MoonPhase.entries[phases[it + 1]]) }
        } else {
            val res = ArrayList<MoonPhaseDateTime>(5)

            for (i in indices) {
                val dateTime = moon[i].dateTime
                val a = dateTime.minusMinutes(stepSizeInMinutes.toLong())
                val b = dateTime.plusMinutes(stepSizeInMinutes.toLong())
                res.addAll(compute(1, a, b, site, offsetInMinutes))
            }

            return res
        }
    }

    companion object {

        private val QUANTITIES = arrayOf(HorizonsQuantity.OBSERVER_ECLIPTIC_LONGITUDE)

        private fun HorizonsElement.eclipticLongitude(): Double {
            return this[HorizonsQuantity.OBSERVER_ECLIPTIC_LONGITUDE]!!.toDouble().deg
        }
    }
}
