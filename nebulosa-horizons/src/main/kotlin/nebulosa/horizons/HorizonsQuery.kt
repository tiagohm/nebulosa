package nebulosa.horizons

import nebulosa.math.Angle
import nebulosa.math.Distance
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// https://ssd-api.jpl.nasa.gov/doc/horizons.html

class HorizonsQuery {

    fun observer(
        command: String,
        startTime: LocalDateTime, endTime: LocalDateTime,
        longitude: Angle, latitude: Angle, elevation: Distance = Distance.ZERO,
        refraction: ApparentRefractionCorrection = ApparentRefractionCorrection.AIRLESS,
        extraPrecision: Boolean = true,
        stepSize: Duration = Duration.ofMinutes(60L),
        vararg quantities: HorizonsQuantity = HorizonsQuantity.values(),
    ): HorizonsEphemeris {
        require(stepSize.toMinutes() > 0) { "stepSize must be greater or equal to 1 minute" }

        return HTTP_CLIENT.newCall(
            Request.Builder()
                .url(
                    """
                https://ssd.jpl.nasa.gov/api/horizons.api?format=text
                &MAKE_EPHEM=YES
                &COMMAND='$command'
                &EPHEM_TYPE=OBSERVER
                &CENTER='coord@399'
                &COORD_TYPE=GEODETIC
                &SITE_COORD='${longitude.degrees},${latitude.degrees},${elevation.kilometers}'
                &START_TIME='${DATE_TIME_FORMAT.format(startTime)}'
                &STOP_TIME='${DATE_TIME_FORMAT.format(endTime)}'
                &STEP_SIZE='${stepSize.seconds}'
                &QUANTITIES='${quantities.map { it.code }.joinToString(",")}'
                &REF_SYSTEM='ICRF'
                &CAL_FORMAT='CAL'
                &TIME_DIGITS='MINUTES'
                &ANG_FORMAT='DEG'
                &APPARENT='$refraction'
                &RANGE_UNITS='AU'
                &SUPPRESS_RANGE_RATE='NO'
                &SKIP_DAYLT='NO'
                &SOLAR_ELONG='0,180'
                &EXTRA_PREC='${if (extraPrecision) "YES" else "NO"}'
                &OBJ_DATA='NO'
                &CSV_FORMAT='NO'
                &ELEV_CUT='-90'
            """.trimIndent()
                ).build()
        ).execute()
            .use { HorizonsEphemeris.parse(it.body.byteStream(), *quantities) }
    }

    companion object {

        @JvmStatic private val HTTP_CLIENT = OkHttpClient.Builder().build()
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    }
}
