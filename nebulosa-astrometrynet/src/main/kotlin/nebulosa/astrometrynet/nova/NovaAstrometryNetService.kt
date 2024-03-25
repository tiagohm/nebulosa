package nebulosa.astrometrynet.nova

import nebulosa.fits.FitsIO
import nebulosa.image.Image
import nebulosa.retrofit.RetrofitService
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import retrofit2.Call
import retrofit2.create
import java.nio.file.Path
import java.util.*
import kotlin.io.path.extension

class NovaAstrometryNetService(
    url: String = "",
    httpClient: OkHttpClient? = null,
) : RetrofitService(url.ifBlank { URL }, httpClient) {

    private val service by lazy { retrofit.create<NovaAstrometryNet>() }

    fun login(apiKey: String): Call<Session> {
        return FormBody.Builder()
            .add("request-json", mapper.writeValueAsString(mapOf("apikey" to apiKey)))
            .build()
            .let(service::login)
    }

    fun uploadFromUrl(upload: Upload): Call<Submission> {
        return FormBody.Builder()
            .add("request-json", mapper.writeValueAsString(upload))
            .build()
            .let(service::uploadFromUrl)
    }

    fun uploadFromFile(path: Path, upload: Upload): Call<Submission> {
        val requestJsonBody = mapper.writeValueAsBytes(upload).toRequestBody(TEXT_PLAIN_MEDIA_TYPE)

        val fileName = "%s.%s".format(UUID.randomUUID(), path.extension)
        val fileBody = path.toFile().asRequestBody(OCTET_STREAM_MEDIA_TYPE)

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(MultipartBody.Part.createFormData("request-json", null, requestJsonBody))
            .addPart(MultipartBody.Part.createFormData("file", fileName, fileBody))
            .build()

        return service.uploadFromFile(body)
    }

    fun uploadFromImage(image: Image, upload: Upload): Call<Submission> {
        val requestJsonBody = mapper.writeValueAsBytes(upload).toRequestBody(TEXT_PLAIN_MEDIA_TYPE)

        val fileName = "%s.fits".format(UUID.randomUUID())
        val fileBody = object : RequestBody() {

            override fun contentType() = OCTET_STREAM_MEDIA_TYPE

            override fun writeTo(sink: BufferedSink) {
                FitsIO.write(sink, image.hdu())
            }
        }

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(MultipartBody.Part.createFormData("request-json", null, requestJsonBody))
            .addPart(MultipartBody.Part.createFormData("file", fileName, fileBody))
            .build()

        return service.uploadFromFile(body)
    }

    fun submissionStatus(subId: Int) = service.submissionStatus(subId)

    fun jobStatus(jobId: Int) = service.jobStatus(jobId)

    fun jobCalibration(jobId: Int) = service.jobCalibration(jobId)

    fun wcs(jobId: Int) = service.wcs(jobId)

    companion object {

        const val URL = "https://nova.astrometry.net/"

        @JvmStatic private val TEXT_PLAIN_MEDIA_TYPE = "text/plain".toMediaType()
        @JvmStatic private val OCTET_STREAM_MEDIA_TYPE = "application/octet-stream".toMediaType()
    }
}
