package nebulosa.api.notification

import com.fasterxml.jackson.annotation.JsonValue

enum class Severity(@field:JsonValue @JvmField val value: String) {
    INFO("info"),
    SUCCESS("success"),
    WARNING("warning"),
    ERROR("error"),
}
