package nebulosa.api.image

import nebulosa.fits.fits
import nebulosa.imaging.Image
import nebulosa.plate.solving.PlateSolution
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class ImageBucket {

    private val bucket = HashMap<Path, Pair<Image, PlateSolution?>>(256)

    @Synchronized
    fun put(path: Path, image: Image, solution: PlateSolution? = null) {
        bucket[path] = image to solution
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
        val image = path.fits().use { openedImage?.first?.load(it) ?: Image.open(it, debayer) }
        put(path, image, solution ?: PlateSolution.from(image.header))
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
