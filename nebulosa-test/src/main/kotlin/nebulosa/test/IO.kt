@file:JvmName("Extensions")
@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.test

import okio.ByteString.Companion.toByteString
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.createParentDirectories
import kotlin.io.path.readBytes

inline fun ByteArray.md5() = toByteString().md5().hex()
inline fun Path.md5() = readBytes().md5() // TODO: Improve it. Remove readBytes()

fun BufferedImage.save(name: String): Pair<Path, String> {
    val path = dataDirectory.concat("test", "$name.png").createParentDirectories()
    ImageIO.write(this, "PNG", path.toFile())
    return path to path.md5().also { println("$name: $it") }
}
