package nebulosa.phd2.client.commands

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import nebulosa.phd2.client.events.StarPosition
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import kotlin.io.encoding.Base64

data class StarImage(
    val frame: Int = 0,
    val width: Int = 0, val height: Int = 0,
    @field:JsonDeserialize(using = StarPosition.Deserializer::class)
    @field:JsonProperty("star_pos") val starPosition: StarPosition = StarPosition.ZERO,
    val pixels: String = "",
) {

    fun decodeImage(): BufferedImage {
        val pixels = Base64.decode(pixels)
        val image = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
        val bytes = (image.raster.dataBuffer as DataBufferByte).data
        for (i in pixels.indices step 2) bytes[i / 2] = pixels[i]
        return image
    }
}
