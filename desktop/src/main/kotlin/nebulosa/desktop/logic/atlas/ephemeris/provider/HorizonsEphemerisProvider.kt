package nebulosa.desktop.logic.atlas.ephemeris.provider

import nebulosa.horizons.HorizonsEphemeris
import nebulosa.horizons.HorizonsQuantity
import nebulosa.horizons.HorizonsService
import nebulosa.nova.position.GeographicPosition
import nebulosa.sbd.SmallBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicReference

@Service
class HorizonsEphemerisProvider : EphemerisProvider<Any> {

    @Autowired private lateinit var horizonsService: HorizonsService

    private val currentStartTime = AtomicReference<LocalDateTime>()
    private val ephemerisCache = hashMapOf<GeographicPosition, MutableMap<Any, HorizonsEphemeris>>()

    override fun compute(
        target: Any,
        position: GeographicPosition,
        force: Boolean,
        vararg quantities: HorizonsQuantity,
    ): HorizonsEphemeris? {
        val now = OffsetDateTime.now()
        val offset = now.offset.totalSeconds.toLong()
        val startTime = if (now.hour >= 12) LocalDateTime.of(now.year, now.month, now.dayOfMonth, 12, 0, 0, 0).minusSeconds(offset)
        else LocalDateTime.of(now.year, now.month, now.dayOfMonth, 12, 0, 0, 0).minusDays(1L).minusSeconds(offset)

        if (currentStartTime.get() == startTime && !force && position in ephemerisCache && target in ephemerisCache[position]!!) {
            return ephemerisCache[position]!![target]
        }

        val endTime = startTime.plusDays(1L)

        LOG.info("retrieving ephemeris from JPL Horizons. target={}, startTime={}, endTime={}", target, startTime, endTime)

        currentStartTime.set(startTime)

        if (position !in ephemerisCache) ephemerisCache[position] = hashMapOf()

        val call = when (target) {
            is SmallBody -> {
                val elements = target.orbit!!.elements
                val physical = target.physical
                val eccentricity = elements.first { it.name == "e" }.value!!
                val longitudeOfAscendingNode = elements.first { it.name == "om" }.value!!
                val argumentOfPerihelion = elements.first { it.name == "w" }.value!!
                val inclination = elements.first { it.name == "i" }.value!!
                val perihelionDistance = elements.first { it.name == "q" }.value!!
                val perihelionJulianDayNumber = elements.first { it.name == "tp" }.value!!
                val meanAnomaly = elements.first { it.name == "ma" }.value!!
                val semiMajorAxis = elements.first { it.name == "a" }.value!!
                val meanMotion = elements.first { it.name == "n" }.value!!
                val absoluteMagnitude = physical?.firstOrNull { it.name == "H" }?.value
                    ?: elements.firstOrNull { it.name == "H" }?.value

                horizonsService
                    .observerWithOsculationElements(
                        target.body!!.fullname,
                        target.orbit!!.epoch.toString(),
                        eccentricity, longitudeOfAscendingNode, argumentOfPerihelion,
                        inclination, perihelionDistance, perihelionJulianDayNumber,
                        meanAnomaly, semiMajorAxis, meanMotion, absoluteMagnitude,
                        position.longitude, position.latitude, position.elevation,
                        startTime, endTime,
                        extraPrecision = true,
                        quantities = arrayOf(*QUANTITIES, *quantities),
                    )
            }
            is String -> {
                horizonsService
                    .observer(
                        target,
                        position.longitude, position.latitude, position.elevation,
                        startTime, endTime,
                        extraPrecision = true,
                        quantities = arrayOf(*QUANTITIES, *quantities),
                    )
            }
            else -> return null
        }

        return call.execute().body()
            ?.also { ephemerisCache[position]!![target] = it }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(HorizonsEphemerisProvider::class.java)

        @JvmStatic private val QUANTITIES = arrayOf(
            HorizonsQuantity.ASTROMETRIC_RA, HorizonsQuantity.ASTROMETRIC_DEC,
            HorizonsQuantity.APPARENT_RA, HorizonsQuantity.APPARENT_DEC,
            HorizonsQuantity.APPARENT_AZ, HorizonsQuantity.APPARENT_ALT,
            HorizonsQuantity.VISUAL_MAGNITUDE, HorizonsQuantity.ONE_WAY_LIGHT_TIME,
        )
    }
}
