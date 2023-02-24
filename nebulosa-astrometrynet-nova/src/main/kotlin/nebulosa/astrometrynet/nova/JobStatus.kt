package nebulosa.astrometrynet.nova

import com.fasterxml.jackson.annotation.JsonProperty

data class JobStatus(@field:JsonProperty("status") val status: String = "")
