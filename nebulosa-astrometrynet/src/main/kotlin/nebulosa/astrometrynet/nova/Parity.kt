package nebulosa.astrometrynet.nova

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.NUMBER)
enum class Parity {
    POSITIVE,
    NEGATIVE,
    BOTH,
}
