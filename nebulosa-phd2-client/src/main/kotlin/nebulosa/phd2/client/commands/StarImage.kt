package nebulosa.phd2.client.commands

import com.fasterxml.jackson.annotation.JsonProperty

data class StarImage(
    val frame: Int = 0,
    val width: Int = 0, val height: Int = 0,
    @field:JsonProperty("star_pos") val starPosition: StarPosition = StarPosition.ZERO,
    val pixels: String = "",
)
