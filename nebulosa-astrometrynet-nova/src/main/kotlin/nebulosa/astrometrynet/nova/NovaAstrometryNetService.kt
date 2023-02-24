package nebulosa.astrometrynet.nova

import nebulosa.retrofit.RetrofitService
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import java.io.File
import java.util.*

class NovaAstrometryNetService(url: String = "https://nova.astrometry.net/api/") : RetrofitService(url), NovaAstrometryNet {

    private val service = retrofit.create(NovaAstrometryNet::class.java)

    override fun login(body: FormBody) = service.login(body)

    fun login(apiKey: String): Call<Session> {
        val body = FormBody.Builder()
            .add("request-json", mapper.writeValueAsString(mapOf("apikey" to apiKey)))
            .build()

        return login(body)
    }

    override fun uploadFromUrl(body: FormBody) = service.uploadFromUrl(body)

    fun uploadFromUrl(upload: Upload): Call<Submission> {
        val body = FormBody.Builder()
            .add("request-json", mapper.writeValueAsString(upload))
            .build()

        return uploadFromUrl(body)
    }

    override fun uploadFromFile(body: MultipartBody) = service.uploadFromFile(body)

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

    override fun submissionStatus(subId: Int) = service.submissionStatus(subId)

    override fun jobStatus(jobId: Int) = service.jobStatus(jobId)

    override fun jobCalibration(jobId: Int) = service.jobCalibration(jobId)

    companion object {

        @JvmStatic private val TEXT_PLAIN_MEDIA_TYPE = "text/plain".toMediaType()
        @JvmStatic private val OCTET_STREAM_MEDIA_TYPE = "application/octet-stream".toMediaType()
    }
}
