package nebulosa.query.vizier

import okhttp3.ConnectionPool
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Path.Companion.toPath
import java.util.concurrent.TimeUnit

class VizierQuery(private val serverUri: String = "http://vizier.cfa.harvard.edu") {

    /**
     * Search Vizier for catalogs based on a set of keywords.
     */
    fun catalogs(
        vararg keywords: String,
        ucd: String = "",
    ) {
        val body = FormBody.Builder()
            .add("-words", keywords.joinToString(" "))
            .add("-meta.all", "1")
            .add("-ucd", ucd)
            .build()

        val request = Request.Builder()
            .post(body)
            .url("$serverUri/viz-bin/asu-fits")
            .build()

        CLIENT.newCall(request).execute().body.use {
            it.byteStream().transferTo("/home/tiagohm/Área de Trabalho/Vizier.fits".toPath().toFile().outputStream())
        }
    }

    companion object {

        @JvmStatic private val CONNECTION_POOL = ConnectionPool(8, 5L, TimeUnit.MINUTES)
        @JvmStatic private val CLIENT = OkHttpClient.Builder().connectionPool(CONNECTION_POOL).build()
    }
}
