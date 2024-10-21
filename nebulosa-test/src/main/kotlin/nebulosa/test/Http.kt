@file:JvmName("Http")

package nebulosa.test

import nebulosa.hips2fits.Hips2FitsService
import nebulosa.hips2fits.HipsSurvey
import nebulosa.io.transferAndCloseOutput
import nebulosa.math.Angle
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.outputStream

val HTTP_CLIENT = OkHttpClient.Builder()
    .readTimeout(60L, TimeUnit.SECONDS)
    .writeTimeout(60L, TimeUnit.SECONDS)
    .connectTimeout(60L, TimeUnit.SECONDS)
    .callTimeout(60L, TimeUnit.SECONDS)
    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
    .build()

private val String.extensionFromUrl
    get() = lastIndexOf('.').let { if (it >= 0) substring(it + 1) else this }

fun download(url: String, extension: String = url.extensionFromUrl): Path {
    require(extension.isNotBlank())

    return synchronized(url) {
        val name = url.encodeToByteArray().md5()
        val path = cacheDirectory.concat("$name.$extension")

        if (!path.exists() || path.fileSize() <= 0L) {
            val request = Request.Builder().get().url(url).build()
            val call = HTTP_CLIENT.newCall(request)

            call.execute().use {
                it.body?.byteStream()?.transferAndCloseOutput(path.outputStream())
            }
        } else {
            println("$path is already downloaded")
        }

        path
    }
}

private val HIPS_SERVICE = Hips2FitsService(httpClient = HTTP_CLIENT)
private val CDS_P_DSS2_NIR = HipsSurvey("CDS/P/DSS2/NIR")

fun downloadFits(centerRA: Angle, centerDEC: Angle, fov: Angle): Path {
    val name = "$centerRA@$centerDEC@$fov".toByteArray().md5()
    val path = cacheDirectory.concat(name)

    if (!path.exists() || path.fileSize() <= 0L) {
        HIPS_SERVICE
            .query(CDS_P_DSS2_NIR.id, centerRA, centerDEC, 1280, 720, 0.0, fov)
            .execute()
            .body()!!
            .use { it.byteStream().transferAndCloseOutput(path.outputStream()) }
    }

    return path
}
