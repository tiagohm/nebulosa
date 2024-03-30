package nebulosa.astrobin.api

import com.fasterxml.jackson.annotation.JsonValue

enum class SensorColor(@JsonValue val code: String) {
    MONO("M"),
    COLOR("C"),
}
