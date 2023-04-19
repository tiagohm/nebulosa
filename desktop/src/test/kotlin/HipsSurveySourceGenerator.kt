import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.hips2fits.HipsSurvey
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

object HipsSurveySourceGenerator : Comparator<HipsSurvey> {

    @JvmStatic private val REGIMES = listOf("Optical", "Infrared", "X-ray", "Gamma-ray")

    override fun compare(a: HipsSurvey, b: HipsSurvey): Int {
        if (a.regime == b.regime) return a.id.compareTo(b.id)
        return REGIMES.indexOf(a.regime) - REGIMES.indexOf(b.regime)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val request = Request.Builder()
            .get()
            .url("https://alasky.u-strasbg.fr/MocServer/query?expr=hips_service_url*%3D*alasky*%20%26%26%20dataproduct_type%3Dimage&get=record&fmt=json")
            .build()

        val client = OkHttpClient.Builder()
            .cache(Cache(File(".cache"), 1024 * 1024 * 32))
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
                val filteredData = data.filter(::filter)
                val sortedData = filteredData.sortedWith(HipsSurveySourceGenerator)

                for (item in sortedData) {
                    val params = arrayOf(item.id, item.category, item.frame, item.regime)
                    println("""HipsSurvey(${params.joinToString(", ") { "\"$it\"" }}, ${item.bitPix}, ${item.pixelScale}, ${item.skyFraction}),""")
                }
            }
    }

    @JvmStatic
    private fun filter(source: HipsSurvey): Boolean {
        return source.skyFraction >= 0.99
                && source.frame == "equatorial"
                && source.category.startsWith("Image/")
    }
}
