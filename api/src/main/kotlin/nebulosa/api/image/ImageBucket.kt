package nebulosa.api.image

import nebulosa.imaging.Image
import nebulosa.platesolving.Calibration
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class ImageBucket {

    private val bucket = HashMap<Path, Pair<Image, Calibration?>>(256)

    @Synchronized
    fun put(path: Path, image: Image, calibration: Calibration? = null) {
        remove(path)
        bucket[path] = image to calibration
    }

    @Synchronized
    fun put(path: Path, calibration: Calibration): Boolean {
        val item = bucket[path] ?: return false
        bucket[path] = item.first to calibration
        return true
    }

    @Synchronized
    fun open(path: Path, debayer: Boolean = true, calibration: Calibration? = null): Image {
        remove(path)
        val image = Image.open(path, debayer)
        put(path, image, calibration)
        return image
    }

    @Synchronized
    fun remove(path: Path) {
        bucket.remove(path)
    }

    operator fun get(path: Path): Pair<Image, Calibration?>? {
        return bucket[path]
    }

    operator fun contains(path: Path): Boolean {
        return path in bucket
    }

    operator fun contains(image: Image): Boolean {
        return bucket.any { it.value.first === image }
    }

    operator fun contains(calibration: Calibration): Boolean {
        return bucket.any { it.value.second === calibration }
    }
}
