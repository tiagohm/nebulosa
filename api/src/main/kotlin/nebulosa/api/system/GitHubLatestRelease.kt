package nebulosa.api.system

import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubLatestRelease(
    @JvmField @field:JsonProperty("tag_name") val version: String = "",
    @JvmField @field:JsonProperty("name") val name: String = "",
    @JvmField @field:JsonProperty("draft") val draft: Boolean = false,
    @JvmField @field:JsonProperty("prerelease") val preRelease: Boolean = false,
)
