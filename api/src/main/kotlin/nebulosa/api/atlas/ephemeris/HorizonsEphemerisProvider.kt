package nebulosa.api.atlas.ephemeris

import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsQuantity
import nebulosa.horizons.HorizonsService
import nebulosa.horizons.NonUniqueObjectException
import nebulosa.log.loggerFor
import nebulosa.nova.position.GeographicPosition
import nebulosa.sbd.SmallBody
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class HorizonsEphemerisProvider(private val horizonsService: HorizonsService) : CachedEphemerisProvider<Any>() {

    override fun compute(
        target: Any,
        position: GeographicPosition,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<HorizonsElement> {
        return when (target) {
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
                        quantities = QUANTITIES,
                    ).execute()
            }
            is String -> {
                if (target.startsWith("TLE@")) {
                    horizonsService
                        .observerWithTLE(
                            target.substring(4),
                            position.longitude, position.latitude, position.elevation,
                            startTime, endTime,
                            extraPrecision = true,
                            quantities = QUANTITIES,
                        ).execute()
                } else {
                    try {
                        horizonsService
                            .observer(
                                target,
                                position.longitude, position.latitude, position.elevation,
                                startTime, endTime,
                                extraPrecision = true,
                                quantities = QUANTITIES,
                            ).execute()
                    } catch (e: NonUniqueObjectException) {
                        LOG.warn("non unique object. target={}, matches={}", target, e.recordItems)

                        horizonsService
                            .observer(
                                "$target;CAP;NOFRAG".replace(";;", ";"),
                                position.longitude, position.latitude, position.elevation,
                                startTime, endTime,
                                extraPrecision = true,
                                quantities = QUANTITIES,
                            ).execute()
                    }
                }
            }
            else -> return emptyList()
        }.body() ?: emptyList()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<HorizonsEphemerisProvider>()

        @JvmStatic private val QUANTITIES = arrayOf(
            HorizonsQuantity.ASTROMETRIC_RA, HorizonsQuantity.ASTROMETRIC_DEC,
            HorizonsQuantity.APPARENT_RA, HorizonsQuantity.APPARENT_DEC,
            HorizonsQuantity.APPARENT_AZ, HorizonsQuantity.APPARENT_ALT,
            HorizonsQuantity.VISUAL_MAGNITUDE, HorizonsQuantity.ONE_WAY_LIGHT_TIME,
            HorizonsQuantity.ILLUMINATED_FRACTION, HorizonsQuantity.SUN_OBSERVER_TARGET_ELONGATION_ANGLE,
            HorizonsQuantity.CONSTELLATION,
        )
    }
}
