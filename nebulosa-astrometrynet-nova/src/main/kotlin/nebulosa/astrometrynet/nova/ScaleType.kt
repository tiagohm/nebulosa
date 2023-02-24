package nebulosa.astrometrynet.nova

import com.fasterxml.jackson.annotation.JsonValue

enum class ScaleType(@field:JsonValue private val value: String) {
    UPPER_LOWER("ul"),
    ESTIMATIVE_ERROR("ev"),
}
