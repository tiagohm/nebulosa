package nebulosa.api.atlas

import nebulosa.constants.AU_KM
import nebulosa.sbd.SmallBodyIdentified
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

data class CloseApproach(
    val name: String = "",
    val designation: String = "",
    val dateTime: Long = 0,
    val distance: Double = 0.0,
    val absoluteMagnitude: Double = 0.0,
) {

    companion object {

        @JvmStatic val EMPTY = CloseApproach()

        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-LLL-dd HH:mm", Locale.ENGLISH)

        @JvmStatic
        fun of(body: SmallBodyIdentified): List<CloseApproach> {
            val data = ArrayList<CloseApproach>(body.count)

            val nameIdx = body.fields.indexOf("fullname")
            val desIdx = body.fields.indexOf("des")
            val cdIdx = body.fields.indexOf("cd")
            val distIdx = body.fields.indexOf("dist")
            val hIdx = body.fields.indexOf("h")

            for (entry in body.data) {
                val name = entry[nameIdx].trim()
                val designation = entry[desIdx].trim()
                val dateTime = LocalDateTime.parse(entry[cdIdx].trim(), DATE_TIME_FORMAT).toEpochSecond(ZoneOffset.UTC) * 1000L
                val distance = entry[distIdx].trim().toDouble() * AU_KM / 384400.0
                val absoluteMagnitude = entry[hIdx].trim().toDouble()

                data.add(CloseApproach(name, designation, dateTime, distance, absoluteMagnitude))
            }

            return data
        }
    }
}
