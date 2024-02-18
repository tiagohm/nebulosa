package nebulosa.alpaca.api

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
enum class AxisType {
    PRIMARY,
    SECONDARY,
    TERTIARY,
}
