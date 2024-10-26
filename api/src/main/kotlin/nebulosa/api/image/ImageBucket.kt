package nebulosa.api.image

import nebulosa.fits.fits
import nebulosa.image.Image
import nebulosa.log.di
import nebulosa.log.loggerFor
import nebulosa.platesolver.PlateSolution
import nebulosa.xisf.xisf
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.io.path.extension

class ImageBucket(scheduledExecutorService: ScheduledExecutorService) {

    data class OpenedImage(
        @JvmField var image: Image? = null,
        @JvmField var solution: PlateSolution? = null,
        @JvmField val debayer: Boolean = true,
        @JvmField var openedAt: Long = System.currentTimeMillis(),
    )

    private val bucket = ConcurrentHashMap<Path, OpenedImage>(8)

    init {
        scheduledExecutorService.scheduleAtFixedRate(::deleteUnusedImages, IMAGE_TIMEOUT, IMAGE_TIMEOUT, TimeUnit.MILLISECONDS)
    }

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

    private fun deleteUnusedImages() {
        val currentTime = System.currentTimeMillis()

        synchronized(this) {
            for ((path, image) in bucket) {
                if (image.image != null && currentTime - image.openedAt >= IMAGE_TIMEOUT) {
                    image.image = null
                    LOG.di("image at {} has been disposed", path)
                }
            }
        }
    }

    companion object {

        private const val IMAGE_TIMEOUT = 1000 * 60 * 5L // 5 min

        private val LOG = loggerFor<ImageBucket>()
    }
}
