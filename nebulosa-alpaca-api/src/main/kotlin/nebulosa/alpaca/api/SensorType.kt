package nebulosa.alpaca.api

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
enum class SensorType {
    MONOCHROME,
    NO_COLOR,
    RGGB,
    CMYB,
    CMYG2,
    LRGB_TRUESENSE,
}
