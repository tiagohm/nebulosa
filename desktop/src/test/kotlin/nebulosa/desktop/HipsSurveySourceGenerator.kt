package nebulosa.desktop

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.hips2fits.HipsSurvey
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

object HipsSurveySourceGenerator {

    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("desktop/src/main/resources/data/HIPS_SURVEY_SOURCES.json")

        val request = Request.Builder()
            .get()
            .url("https://alasky.u-strasbg.fr/MocServer/query?expr=hips_service_url*%3D*alasky*%20%26%26%20dataproduct_type%3Dimage&get=record&fmt=json")
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(1L, TimeUnit.MINUTES)
            .writeTimeout(1L, TimeUnit.MINUTES)
            .readTimeout(1L, TimeUnit.MINUTES)
            .callTimeout(1L, TimeUnit.MINUTES)
            .build()

        val mapper = ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)



        client.newCall(request)
            .execute()
            .use { response ->
                val body = response.body
                val json = body.bytes().toString(body.contentType()?.charset() ?: Charsets.ISO_8859_1)
                val data = mapper.readValue(json, Array<HipsSurvey>::class.java)
                val filteredData = mapper.writeValueAsBytes(data.filter(::filter))
                file.writeBytes(filteredData)
            }
    }

    @JvmStatic
    private fun filter(source: HipsSurvey): Boolean {
        return source.skyFraction >= 0.99
                && source.frame == "equatorial"
                && source.category.startsWith("Image/")
    }
}
