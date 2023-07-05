package nebulosa.api.services

import jakarta.servlet.http.HttpServletResponse
import nebulosa.api.data.entities.SavedCameraImage
import nebulosa.api.repositories.SavedCameraImageRepository
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.*
import org.springframework.stereotype.Service
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.getLastModifiedTime

@Service
class ImageService(private val savedCameraImageRepository: SavedCameraImageRepository) {

    private val savedImages = ArrayList<SavedCameraImage>(1024)

    fun load(
        path: Path,
        autoStretch: Boolean, shadow: Float, highlight: Float, midtone: Float,
        mirrorHorizontal: Boolean, mirrorVertical: Boolean, invert: Boolean,
        scnrEnabled: Boolean, scnrChannel: ImageChannel, scnrAmount: Float, scnrProtectionMode: ProtectionMethod,
        output: HttpServletResponse,
    ) {
        val image = Image.open(path.toFile())

        val shouldBeTransformed = autoStretch || shadow != 0f || highlight != 1f || midtone != 0.5f
                || mirrorHorizontal || mirrorVertical || invert
                || scnrEnabled

        val algorithms = ArrayList<TransformAlgorithm>(5)

        if (shouldBeTransformed) {
            if (mirrorHorizontal) algorithms.add(HorizontalFlip)
            if (mirrorVertical) algorithms.add(VerticalFlip)
            if (scnrEnabled) algorithms.add(SubtractiveChromaticNoiseReduction(scnrChannel, scnrAmount, scnrProtectionMode))
            if (autoStretch) algorithms.add(AutoScreenTransformFunction)
            else algorithms.add(ScreenTransformFunction(midtone, shadow, highlight))
            if (invert) algorithms.add(Invert)
        }

        val transformedImage = TransformAlgorithm.of(algorithms).transform(image)

        savedImages.add(
            SavedCameraImage(
                path = "$path",
                width = image.width, height = image.height, mono = image.mono,
                savedAt = path.getLastModifiedTime().toMillis(),
            )
        )

        output.contentType = "image/png"

        ImageIO.write(transformedImage, "PNG", output.outputStream)
    }

    fun savedImagesOfCamera(name: String): List<SavedCameraImage> {
        return savedCameraImageRepository.findName(name)
    }

    fun latestSavedImageOfCamera(name: String): SavedCameraImage {
        return savedImagesOfCamera(name).last()
    }

    fun savedImageOfPath(path: Path): SavedCameraImage {
        return with("$path") {
            savedCameraImageRepository.findPath(this)
                ?: savedImages.first { it.path == this }
        }
    }

    fun savedImage(id: Long): SavedCameraImage {
        return savedCameraImageRepository.findId(id)!!
    }

    fun savedImage(name: String, path: Path): SavedCameraImage {
        return savedCameraImageRepository.findNameAndPath(name, "$path")!!
    }
}
