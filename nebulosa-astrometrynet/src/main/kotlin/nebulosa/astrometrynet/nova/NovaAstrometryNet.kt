package nebulosa.astrometrynet.nova

import nebulosa.retrofit.RawAsString
import okhttp3.FormBody
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NovaAstrometryNet {

    @POST("api/login")
    fun login(@Body body: FormBody): Call<Session>

    @POST("api/url_upload")
    fun uploadFromUrl(@Body body: FormBody): Call<Submission>

    @POST("api/upload")
    fun uploadFromFile(@Body body: MultipartBody): Call<Submission>

    @GET("api/submissions/{subId}")
    fun submissionStatus(@Path("subId") subId: Int): Call<SubmissionStatus>

    @GET("api/jobs/{jobId}")
    fun jobStatus(@Path("jobId") jobId: Int): Call<JobStatus>

    @GET("api/jobs/{jobId}/calibration")
    fun jobCalibration(@Path("jobId") jobId: Int): Call<JobCalibration>

    @RawAsString
    @GET("wcs_file/{jobId}")
    fun wcs(@Path("jobId") jobId: Int): Call<String>
}
