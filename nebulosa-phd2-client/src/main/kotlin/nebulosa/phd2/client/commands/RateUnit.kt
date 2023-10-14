package nebulosa.phd2.client.commands

import com.fasterxml.jackson.annotation.JsonValue

enum class RateUnit(@JsonValue val rate: String) {
    ARCSEC_HOUR("arcsec/hr"),
    PIXELS_HOUR("pixels/hr"),
}
