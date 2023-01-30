package nebulosa.guiding.phd2.event

import com.fasterxml.jackson.annotation.JsonProperty

enum class GuideState {
    @JsonProperty("Stopped")
    STOPPED,

    @JsonProperty("Selected")
    SELECTED,

    @JsonProperty("Calibrating")
    CALIBRATIING,

    @JsonProperty("Guiding")
    GUIDING,

    @JsonProperty("LostLock")
    LOST_LOCK,

    @JsonProperty("Paused")
    PAUSED,

    @JsonProperty("Looping")
    LOOPING,
}
