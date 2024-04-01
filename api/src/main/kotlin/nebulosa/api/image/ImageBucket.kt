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

    private val bucket = HashMap<Path, Pair<Image, PlateSolution?>>(256)

    @Synchronized
    fun put(path: Path, image: Image, solution: PlateSolution? = null) {
        bucket[path] = image to (solution ?: PlateSolution.from(image.header))
    }

    @Synchronized
    fun put(path: Path, solution: PlateSolution): Boolean {
        val item = bucket[path] ?: return false
        bucket[path] = item.first to solution
        return true
    }

    @Synchronized
    fun open(path: Path, debayer: Boolean = true, solution: PlateSolution? = null, force: Boolean = false): Image {
        val openedImage = this[path]

        if (openedImage != null && !force) return openedImage.first

        val representation = when (path.extension.lowercase()) {
            "fit", "fits" -> path.fits()
            "xisf" -> path.xisf()
            else -> throw IllegalArgumentException("invalid extension: $path")
        }

        val image = representation.use { openedImage?.first?.load(it) ?: Image.open(it, debayer) }
        put(path, image, solution)
        return image
    }

    @Synchronized
    fun remove(path: Path) {
        bucket.remove(path)
    }

    operator fun get(path: Path): Pair<Image, PlateSolution?>? {
        return bucket[path]
    }

    operator fun contains(path: Path): Boolean {
        return path in bucket
    }

    operator fun contains(image: Image): Boolean {
        return bucket.any { it.value.first === image }
    }

    operator fun contains(solution: PlateSolution): Boolean {
        return bucket.any { it.value.second === solution }
    }
}
