package nebulosa.api.image

import nebulosa.fits.fits
import nebulosa.image.Image
import nebulosa.plate.solving.PlateSolution
import nebulosa.xisf.xisf
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.extension

@Component
class ImageBucket {

    data class OpenedImage(
        @JvmField val image: Image,
        @JvmField var solution: PlateSolution? = null,
        @JvmField val debayer: Boolean = true,
    )

    private val bucket = HashMap<Path, OpenedImage>(256)

    @Synchronized
    fun put(path: Path, image: Image, solution: PlateSolution? = null, debayer: Boolean = true) {
        bucket[path] = OpenedImage(image, solution ?: PlateSolution.from(image.header), debayer)
    }

    @Synchronized
    fun put(path: Path, solution: PlateSolution): Boolean {
        val item = bucket[path] ?: return false
        item.solution = solution
        return true
    }

    @Synchronized
    fun open(path: Path, debayer: Boolean = true, solution: PlateSolution? = null, force: Boolean = false): Image {
        val openedImage = this[path]

        if (openedImage != null && !force && debayer == openedImage.debayer) {
            return openedImage.image
        }

        val representation = when (path.extension.lowercase()) {
            "fit", "fits" -> path.fits()
            "xisf" -> path.xisf()
            else -> throw IllegalArgumentException("invalid extension: $path")
        }

        val image = representation.use { Image.open(it, debayer) }
        put(path, image, solution, debayer)
        return image
    }

    @Synchronized
    fun remove(path: Path) {
        bucket.remove(path)
    }

    operator fun get(path: Path): OpenedImage? {
        return bucket[path]
    }

    operator fun contains(path: Path): Boolean {
        return path in bucket
    }

    operator fun contains(image: Image): Boolean {
        return bucket.any { it.value.image === image }
    }

    operator fun contains(solution: PlateSolution): Boolean {
        return bucket.any { it.value.solution === solution }
    }
}
