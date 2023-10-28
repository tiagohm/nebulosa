package nebulosa.api.atlas

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.services.MessageEvent

data class SkyAtlasUpdateFinished(
    val versionFrom: String?, val versionTo: String,
) : MessageEvent {

    @JsonIgnore override val eventName = "SKY_ATLAS_UPDATE_FINISHED"
}
