package nebulosa.alpaca.api

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
enum class DriveRate {
    SIDEREAL,
    LUNAR,
    SOLAR,
    KING,
}
