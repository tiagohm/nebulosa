package nebulosa.imaging

import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.net.URL
import javax.imageio.ImageIO

/**
 * Extended image file format support for the Java platform.
 *
 * @see <a href="https://github.com/haraldk/TwelveMonkeys">TwelveMonkeys: Additional plug-ins</a>
 */
class ExtendedImage(
    val image: BufferedImage,
) : Image(
    image.width, image.height,
    image.type == TYPE_BYTE_GRAY || image.type == TYPE_USHORT_GRAY,
) {

    constructor(uri: URL) : this(ImageIO.read(uri))

    constructor(file: File) : this(ImageIO.read(file))

    constructor(inputStream: InputStream) : this(ImageIO.read(inputStream))

    override fun read() {
        var idx = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = image.getRGB(x, y)

                if (mono) {
                    // TODO: Fix mono high brightness when write to file.
                    data[idx++] = (rgb and 0xff) / 255f
                } else {
                    data[idx++] = (rgb ushr 16 and 0xff) / 255f
                    data[idx++] = (rgb ushr 8 and 0xff) / 255f
                    data[idx++] = (rgb and 0xff) / 255f
                }
            }
        }
    }
}
