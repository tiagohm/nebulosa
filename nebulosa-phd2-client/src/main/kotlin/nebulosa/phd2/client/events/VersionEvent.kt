package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias

data class VersionEvent(
    @field:JsonAlias("PHDVersion") val version: String = "",
    @field:JsonAlias("PHDSubver") val subVersion: String = "",
    @field:JsonAlias("OverlapSupport") val overlapSupport: Boolean = false,
) : PHD2Event
