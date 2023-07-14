package nebulosa.api.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.repositories.SavedCameraImageRepository
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.*
import org.springframework.stereotype.Service
import java.nio.file.Path
import javax.imageio.ImageIO

@Service
class ImageService(
    private val savedCameraImageRepository: SavedCameraImageRepository,
    private val objectMapper: ObjectMapper,
) {

    private val cachedImages = HashMap<Path, Image>()

    @Synchronized
    fun openImage(
        path: Path,
        cache: Boolean, debayer: Boolean,
        autoStretch: Boolean, shadow: Float, highlight: Float, midtone: Float,
        mirrorHorizontal: Boolean, mirrorVertical: Boolean, invert: Boolean,
        scnrEnabled: Boolean, scnrChannel: ImageChannel, scnrAmount: Float, scnrProtectionMode: ProtectionMethod,
        output: HttpServletResponse,
    ) {
        val image = cachedImages[path] ?: run {
            Image.open(path.toFile(), debayer).also {
                if (cache) {
                    cachedImages[path] = it
                }
            }
        }

        val manualStretch = shadow != 0f || highlight != 1f || midtone != 0.5f
        val shouldBeTransformed = autoStretch || manualStretch
                || mirrorHorizontal || mirrorVertical || invert
                || scnrEnabled

        val transformedImage = if (shouldBeTransformed) {
            val algorithms = ArrayList<TransformAlgorithm>(5)
            if (mirrorHorizontal) algorithms.add(HorizontalFlip)
            if (mirrorVertical) algorithms.add(VerticalFlip)
            if (scnrEnabled) algorithms.add(SubtractiveChromaticNoiseReduction(scnrChannel, scnrAmount, scnrProtectionMode))
            if (manualStretch) algorithms.add(ScreenTransformFunction(midtone, shadow, highlight))
            else if (autoStretch) algorithms.add(AutoScreenTransformFunction)
            if (invert) algorithms.add(Invert)
            TransformAlgorithm.of(algorithms).transform(image)
        } else {
            image
        }

        val info = savedCameraImageRepository.withPath("$path") ?: SavedCameraImageEntity()

        with(info) {
            width = transformedImage.width
            height = transformedImage.height
            mono = transformedImage.mono
            output.addHeader("X-Image-Info", objectMapper.writeValueAsString(this))
        }

        output.contentType = "image/png"

        ImageIO.write(transformedImage, "PNG", output.outputStream)
    }

    @Synchronized
    fun closeImage(path: Path) {
        cachedImages.remove(path)
        System.gc()
    }

    fun imagesOfCamera(name: String): List<SavedCameraImageEntity> {
        return savedCameraImageRepository.withName(name)
    }

    fun latestImageOfCamera(name: String): SavedCameraImageEntity {
        return savedCameraImageRepository.withNameLatest(name)!!
    }

    fun savedImageOfPath(path: Path): SavedCameraImageEntity {
        return savedCameraImageRepository.withPath("$path")!!
    }
}
