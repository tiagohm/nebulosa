package nebulosa.desktop.logic.atlas.ephemeris.provider

import nebulosa.horizons.HorizonsEphemeris
import nebulosa.horizons.HorizonsQuantity
import nebulosa.horizons.HorizonsService
import nebulosa.nova.position.GeographicPosition
import nebulosa.sbd.SmallBody
import nebulosa.time.UTC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class HorizonsEphemerisProvider : AbstractEphemerisProvider<Any>() {

    @Autowired private lateinit var horizonsService: HorizonsService

    override fun compute(
        target: Any,
        position: GeographicPosition,
        timeSpan: List<Pair<UTC, LocalDateTime>>,
        vararg quantities: HorizonsQuantity,
    ): HorizonsEphemeris? {
        val startTime = timeSpan.first().second
        val endTime = timeSpan.last().second

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
    }

    companion object {

        @JvmStatic private val QUANTITIES = arrayOf(
            HorizonsQuantity.ASTROMETRIC_RA, HorizonsQuantity.ASTROMETRIC_DEC,
            HorizonsQuantity.APPARENT_RA, HorizonsQuantity.APPARENT_DEC,
            HorizonsQuantity.APPARENT_AZ, HorizonsQuantity.APPARENT_ALT,
            HorizonsQuantity.VISUAL_MAGNITUDE, HorizonsQuantity.ONE_WAY_LIGHT_TIME,
        )
    }
}
