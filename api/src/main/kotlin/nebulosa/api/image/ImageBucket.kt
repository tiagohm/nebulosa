package nebulosa.api.image

import nebulosa.fits.fits
import nebulosa.image.Image
import nebulosa.log.loggerFor
import nebulosa.platesolver.PlateSolution
import nebulosa.xisf.xisf
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.timer
import kotlin.io.path.extension

@Component
class ImageBucket : AutoCloseable {

    data class OpenedImage(
        @JvmField var image: Image? = null,
        @JvmField var solution: PlateSolution? = null,
        @JvmField val debayer: Boolean = true,
        @JvmField var openedAt: Long = System.currentTimeMillis(),
    )

    private val bucket = ConcurrentHashMap<Path, OpenedImage>(8)
    private val timer = timer("Image Bucket Timer", true, IMAGES_MAX_TIME, IMAGES_MAX_TIME, ::deleteUnusedImages)

    @Synchronized
    fun put(path: Path, image: Image, solution: PlateSolution? = null, debayer: Boolean = true): OpenedImage {
        return OpenedImage(image, solution ?: PlateSolution.from(image.header), debayer).also { bucket[path] = it }
    }

    @Synchronized
    fun put(path: Path, solution: PlateSolution): Boolean {
        val item = bucket[path] ?: return false
        item.solution = solution
        return true
    }

    @Synchronized
    fun open(
        path: Path, debayer: Boolean = this[path]?.debayer != false,
        solution: PlateSolution? = null, force: Boolean = false
    ): OpenedImage {
        val openedImage = this[path]

        if (openedImage != null && !force && debayer == openedImage.debayer) {
            if (openedImage.image != null && solution == null) {
                openedImage.openedAt = System.currentTimeMillis()
                return openedImage
            }
        }

        val representation = when (path.extension.lowercase()) {
            "fit", "fits" -> path.fits()
            "xisf" -> path.xisf()
            else -> throw IllegalArgumentException("invalid extension: $path")
        }

        val image = representation.use { Image.open(it, debayer) }
        return put(path, image, solution ?: openedImage?.solution, debayer)
    }

    @Synchronized
    fun remove(path: Path) {
        bucket.remove(path)
    }

    operator fun get(path: Path): OpenedImage? {
        return bucket[path]
    }

    operator fun contains(path: Path): Boolean {
        return bucket.containsKey(path)
    }

    operator fun contains(image: Image): Boolean {
        return bucket.any { it.value.image === image }
    }

    operator fun contains(solution: PlateSolution): Boolean {
        return bucket.any { it.value.solution === solution }
    }

    override fun close() {
        timer.cancel()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun deleteUnusedImages(task: TimerTask) {
        val currentTime = System.currentTimeMillis()

        synchronized(this) {
            for ((path, image) in bucket) {
                if (currentTime - image.openedAt >= IMAGES_MAX_TIME) {
                    image.image = null
                    LOG.info("image at {} has been disposed", path)
                }
            }
        }
    }

    companion object {

        private const val IMAGES_MAX_TIME = 1000 * 60 * 5L // 5 min

        @JvmStatic private val LOG = loggerFor<ImageBucket>()
    }
}
