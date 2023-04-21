package nebulosa.astrometrynet.nova

import nebulosa.retrofit.RetrofitService
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.create
import java.io.File
import java.util.*

class NovaAstrometryNetService(url: String = URL) : RetrofitService(url) {

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

    fun uploadFromFile(file: File, upload: Upload): Call<Submission> {
        val requestJsonBody = mapper.writeValueAsBytes(upload).toRequestBody(TEXT_PLAIN_MEDIA_TYPE)

        val fileName = "%s.%s".format(UUID.randomUUID(), file.extension)
        val fileBody = file.asRequestBody(OCTET_STREAM_MEDIA_TYPE)

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
