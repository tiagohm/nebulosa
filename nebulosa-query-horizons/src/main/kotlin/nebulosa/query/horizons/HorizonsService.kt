package nebulosa.query.horizons

import nebulosa.query.QueryService
import retrofit2.Call
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HorizonsService : QueryService("https://ssd.jpl.nasa.gov/api/"), Horizons {

    private val service = retrofit.create(Horizons::class.java)

    override fun spk(command: String, startTime: String, endTime: String): Call<SpkFile> {
        return service.spk(command, startTime, endTime)
    }

    fun spk(id: Int, startTime: LocalDateTime, endTime: LocalDateTime): Call<SpkFile> {
        return spk("'DES=$id;'", "'%s'".format(startTime.format(DATE_TIME_FORMAT)), "'%s'".format(endTime.format(DATE_TIME_FORMAT)))
    }

    companion object {

        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }
}
