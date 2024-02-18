package nebulosa.alpaca.api

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
enum class EquatorialCoordinateType {
    OTHER,
    TOPOCENTRIC,
    J2000,
    J2050,
    B1950,
}
