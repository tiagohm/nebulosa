package nebulosa.desktop

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

object MaterialDesignIconGenerator {

    data class MaterialDesignIcon(
        @field:JsonProperty("id") val id: String = "",
        @field:JsonProperty("name") val name: String = "",
        @field:JsonProperty("codepoint") val codepoint: String = "",
        @field:JsonProperty("deprecated") val deprecated: Boolean = false,
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("desktop/src/main/resources/data/MaterialDesignIcon.csv")

        val request = Request.Builder()
            .get()
            .url("https://raw.githubusercontent.com/Templarian/MaterialDesign-SVG/master/meta.json")
            .build()

        val client = OkHttpClient.Builder()
            .cache(Cache(File("../.cache"), 1024 * 1024 * 32))
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
                val json = body.bytes().toString(body.contentType()?.charset() ?: Charsets.UTF_8)
                val data = mapper.readValue(json, Array<MaterialDesignIcon>::class.java)
                val writer = file.outputStream().bufferedWriter()

                for (item in data) {
                    // if (item.deprecated) continue
                    writer.write("${item.name};${convert(item.codepoint)}\n")
                }

                writer.flush()
                writer.close()
            }
    }

    @JvmStatic
    private fun convert(text: String): String {
        val s = text.toInt(16)

        return if (s in 0x10000..0x10FFFF) {
            val hi = (s - 0x10000) / 0x400 + 0xD800
            val lo = (s - 0x10000) % 0x400 + 0xDC00
            String(charArrayOf(hi.toChar(), lo.toChar()))
        } else {
            s.toChar().toString()
        }
    }
}
