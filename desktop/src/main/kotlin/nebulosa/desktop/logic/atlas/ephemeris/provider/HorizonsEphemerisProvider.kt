package nebulosa.desktop.logic.atlas.ephemeris.provider

import nebulosa.constants.DAYSEC
import nebulosa.horizons.HorizonsEphemeris
import nebulosa.horizons.HorizonsQuantity
import nebulosa.horizons.HorizonsService
import nebulosa.nova.position.GeographicPosition
import nebulosa.time.TimeYMDHMS
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Service
class HorizonsEphemerisProvider : EphemerisProvider<String> {

    @Autowired private lateinit var horizonsService: HorizonsService

    @Volatile private var time: LocalDateTime? = null
    private val cache = hashMapOf<GeographicPosition, MutableMap<String, HorizonsEphemeris>>()

    override fun compute(
        target: String,
        position: GeographicPosition,
        force: Boolean,
    ): HorizonsEphemeris? {
        val now = OffsetDateTime.now()
        val offset = now.offset.totalSeconds / DAYSEC
        val startTime = (TimeYMDHMS(now.year, now.monthValue, now.dayOfMonth, 12) - offset).asDateTime().withSecond(0).withNano(0)

        if (time == startTime && !force && position in cache && target in cache[position]!!) {
            return cache[position]!![target]
        }

        val endTime = startTime.plusDays(1L)

        LOG.info("retrieving ephemeris from JPL Horizons. target={}, startTime={}, endTime={}", target, startTime, endTime)

        time = startTime

        if (position !in cache) cache[position] = hashMapOf()

        return horizonsService
            .observer(
                target,
                position.longitude, position.latitude, position.elevation,
                startTime, endTime,
                extraPrecision = true,
                quantities = QUANTITIES,
            ).execute().body()
            ?.also { cache[position]!![target] = it }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(HorizonsEphemerisProvider::class.java)

        @JvmStatic private val QUANTITIES = arrayOf(
            HorizonsQuantity.ASTROMETRIC_RA, HorizonsQuantity.ASTROMETRIC_DEC,
            HorizonsQuantity.APPARENT_RA, HorizonsQuantity.APPARENT_DEC,
            HorizonsQuantity.APPARENT_AZ, HorizonsQuantity.APPARENT_ALT,
        )
    }
}
