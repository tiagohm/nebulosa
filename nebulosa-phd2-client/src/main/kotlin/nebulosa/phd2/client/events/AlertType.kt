package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonValue

enum class AlertType(@JsonValue val type: String) {
    INFO("info"),
    QUESTION("question"),
    WARNING("warning"),
    ERROR("error"),
}
