package nebulosa.astrometrynet.nova

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class SubmissionStatus(
    @field:JsonProperty("processing_started")
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = NovaAstrometryNet.DATE_TIME_PATTERN)
    val processingStarted: LocalDateTime = LocalDateTime.MIN,
    @field:JsonProperty("processing_finished")
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = NovaAstrometryNet.DATE_TIME_PATTERN)
    val processingFinished: LocalDateTime = LocalDateTime.MIN,
    @field:JsonProperty("job_calibrations") val jobCalibrations: Array<IntArray> = emptyArray(),
    @field:JsonProperty("jobs") val jobs: IntArray = IntArray(0),
    @field:JsonProperty("user") val user: Int = 0,
    @field:JsonProperty("user_images") val userImages: IntArray = IntArray(0),
) {

    val started
        get() = jobs.isNotEmpty()

    val solved
        get() = jobCalibrations.isNotEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SubmissionStatus) return false

        if (processingStarted != other.processingStarted) return false
        if (processingFinished != other.processingFinished) return false
        if (!jobCalibrations.contentDeepEquals(other.jobCalibrations)) return false
        if (!jobs.contentEquals(other.jobs)) return false
        if (user != other.user) return false
        if (!userImages.contentEquals(other.userImages)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = processingStarted.hashCode()
        result = 31 * result + processingFinished.hashCode()
        result = 31 * result + jobCalibrations.contentDeepHashCode()
        result = 31 * result + jobs.contentHashCode()
        result = 31 * result + user
        result = 31 * result + userImages.contentHashCode()
        return result
    }
}
