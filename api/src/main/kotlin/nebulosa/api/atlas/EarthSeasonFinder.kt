package nebulosa.api.atlas

import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsQuantity
import nebulosa.horizons.HorizonsService
import nebulosa.horizons.ObservingSite
import nebulosa.nova.almanac.DiscreteFunction
import nebulosa.nova.almanac.findDiscrete
import java.time.LocalDateTime

class EarthSeasonFinder(private val horizonsService: HorizonsService) {

    fun find(year: Int, offsetInMinutes: Long = 0L): List<EarthSeasonDateTime> {
        val startTime = LocalDateTime.of(year, 1, 1, 0, 0, 0)
        val endTime = LocalDateTime.of(year, 12, 31, 23, 59, 59)
        return compute(1440, startTime, endTime, offsetInMinutes)
    }

    private fun compute(
        stepSizeInMinutes: Int,
        startTime: LocalDateTime, endTime: LocalDateTime,
        offsetInMinutes: Long,
    ): List<EarthSeasonDateTime> {
        val sun = horizonsService.observer(
            "10", ObservingSite.Geocentric.EARTH,
            startTime, endTime,
            stepSizeInMinutes,
            extraPrecision = false,
            quantities = QUANTITIES,
        ).execute().body()!!

        val longitudes = DoubleArray(sun.size) { sun[it].eclipticLongitudeInDegrees() }
        val result = findDiscrete(longitudes, EarthSeasonFinder, Double.MAX_VALUE)

        return if (stepSizeInMinutes == 1) {
            result.x.indices.map { EarthSeasonDateTime(sun[result.i[it]].dateTime.plusMinutes(offsetInMinutes), EarthSeason.entries[result.y[it]]) }
        } else {
            val res = ArrayList<EarthSeasonDateTime>(5)

            for (i in result.i) {
                val a = sun[i].dateTime
                val b = sun[i + 1].dateTime
                res.addAll(compute(1, a, b, offsetInMinutes))
            }

            res
        }
    }

    companion object : DiscreteFunction {

        private val QUANTITIES = arrayOf(HorizonsQuantity.OBSERVER_ECLIPTIC_LONGITUDE)

        private fun HorizonsElement.eclipticLongitudeInDegrees(): Double {
            return this[HorizonsQuantity.OBSERVER_ECLIPTIC_LONGITUDE]!!.toDouble()
        }

        override fun invoke(x: Double): Int {
            return x.toInt() / 90
        }
    }
}
