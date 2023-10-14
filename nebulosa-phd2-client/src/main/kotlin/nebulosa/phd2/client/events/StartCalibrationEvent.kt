package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias

data class StartCalibrationEvent(
    @field:JsonAlias("Mount") val mount: String = "",
) : PHD2Event
