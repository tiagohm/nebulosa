package nebulosa.alpaca.api

import com.fasterxml.jackson.annotation.JsonValue

enum class PierSide(@field:JsonValue val code: Int) {
    UNKNOWN(-1),
    EAST(0),
    WEST(1),
}
