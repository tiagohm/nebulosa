package nebulosa.image.format

import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_BYTE_GRAY
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt

interface ImageHdu : Hdu<ImageData> {

    val width: Int

    val height: Int

    val numberOfChannels: Int

    val isMono
        get() = numberOfChannels == 1

    companion object {

        @JvmStatic
        fun ImageHdu.makeImage(): BufferedImage {
            val type = if (numberOfChannels == 1) TYPE_BYTE_GRAY else TYPE_INT_RGB
            val image = BufferedImage(width, height, type)
            val numberOfPixels = data.numberOfPixels

            if (numberOfChannels == 1) {
                val buffer = (image.raster.dataBuffer as DataBufferByte).data

                repeat(numberOfPixels) {
                    buffer[it] = (data.red[it] * 255f).toInt().toByte()
                }
            } else {
                val buffer = (image.raster.dataBuffer as DataBufferInt).data

                repeat(numberOfPixels) {
                    val red = (data.red[it] * 255f).toInt() and 0xFF
                    val green = (data.green[it] * 255f).toInt() and 0xFF
                    val blue = (data.blue[it] * 255f).toInt() and 0xFF
                    buffer[it] = blue or (green shl 8) or (red shl 16)
                }
            }

            return image
        }
    }
}
