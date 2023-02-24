package nebulosa.astrometrynet.nova

import okhttp3.FormBody
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NovaAstrometryNet {

    @POST("login")
    fun login(@Body body: FormBody): Call<Session>

    @POST("url_upload")
    fun uploadFromUrl(@Body body: FormBody): Call<Submission>

    @POST("upload")
    fun uploadFromFile(@Body body: MultipartBody): Call<Submission>

    @GET("submissions/{subId}")
    fun submissionStatus(@Path("subId") subId: Int): Call<SubmissionStatus>

    @GET("jobs/{jobId}")
    fun jobStatus(@Path("jobId") jobId: Int): Call<JobStatus>

    @GET("jobs/{jobId}/calibration")
    fun jobCalibration(@Path("jobId") jobId: Int): Call<JobCalibration>

    companion object {

        const val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss.SSSSSS"
    }
}
