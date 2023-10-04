package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonValue

enum class State(@JsonValue val state: String) {
    STOPPED("Stopped"),
    SELECTED("Selected"),
    CALIBRATING("Calibrating"),
    GUIDING("Guiding"),
    LOST_LOCK("LostLock"),
    PAUSED("Paused"),
    LOOPING("Looping"),
}
