package nebulosa.api.stacker

import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.fits.fits
import nebulosa.fits.isFits
import nebulosa.stacker.AutoStacker
import nebulosa.xisf.isXisf
import nebulosa.xisf.xisf
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Service
class StackerService {

    fun stack(request: StackingRequest, cancellationToken: CancellationToken = CancellationToken.NONE): Path? {
        require(request.outputDirectory != null && request.outputDirectory.exists() && request.outputDirectory.isDirectory())

        val luminance = request.targets.filter { it.enabled && it.group == StackerGroupType.LUMINANCE }
        val red = request.targets.filter { it.enabled && it.group == StackerGroupType.RED }
        val green = request.targets.filter { it.enabled && it.group == StackerGroupType.GREEN }
        val blue = request.targets.filter { it.enabled && it.group == StackerGroupType.BLUE }
        val mono = request.targets.filter { it.enabled && it.group == StackerGroupType.MONO }
        val rgb = request.targets.filter { it.enabled && it.group == StackerGroupType.RGB }

        val name = "${System.currentTimeMillis()}"

        // Combined LRGB
        return if (luminance.size + red.size + green.size + blue.size > 1) {
            val stacker = request.get()

            cancellationToken.listen { stacker.stop() }

            val stackedLuminancePath = luminance.stack(request, stacker, name, StackerGroupType.LUMINANCE, cancellationToken)
            val stackedRedPath = red.stack(request, stacker, name, StackerGroupType.RED, cancellationToken)
            val stackedGreenPath = green.stack(request, stacker, name, StackerGroupType.GREEN, cancellationToken)
            val stackedBluePath = blue.stack(request, stacker, name, StackerGroupType.BLUE, cancellationToken)

            if (cancellationToken.isCancelled) {
                null
            } else {
                val combinedPath = Path.of("${request.outputDirectory}", "$name.LRGB.fits")
                stacker.combineLRGB(combinedPath, stackedLuminancePath, stackedRedPath, stackedGreenPath, stackedBluePath)
                combinedPath
            }
        }
        // LRGB
        else if (rgb.size > 1 || luminance.size + rgb.size > 1) {
            val stacker = request.get()

            val stackedLuminancePath = luminance.stack(request, stacker, name, StackerGroupType.LUMINANCE, cancellationToken)
            val stackedRGBPath = rgb.stack(request, stacker, name, StackerGroupType.RGB, cancellationToken)

            if (cancellationToken.isCancelled) {
                null
            } else if (stackedLuminancePath != null && stackedRGBPath != null) {
                val combinedPath = Path.of("${request.outputDirectory}", "$name.LRGB.fits")
                stacker.combineLuminance(combinedPath, stackedLuminancePath, stackedRGBPath, false)
                combinedPath
            } else {
                stackedLuminancePath ?: stackedRGBPath
            }
        }
        // MONO
        else if (mono.size > 1 || luminance.size + mono.size > 1) {
            val stacker = request.get()

            val stackedLuminancePath = luminance.stack(request, stacker, name, StackerGroupType.LUMINANCE, cancellationToken)
            val stackedMonoPath = mono.stack(request, stacker, name, StackerGroupType.MONO, cancellationToken)

            if (cancellationToken.isCancelled) {
                null
            } else if (stackedLuminancePath != null && stackedMonoPath != null) {
                val combinedPath = Path.of("${request.outputDirectory}", "$name.LRGB.fits")
                stacker.combineLuminance(combinedPath, stackedLuminancePath, stackedMonoPath, true)
                combinedPath
            } else {
                stackedLuminancePath ?: stackedMonoPath
            }
        } else {
            null
        }
    }

    private fun List<StackingTarget>.stack(
        request: StackingRequest, stacker: AutoStacker,
        name: String, group: StackerGroupType, cancellationToken: CancellationToken,
    ): Path? {
        return if (cancellationToken.isCancelled) {
            null
        } else if (size > 1) {
            val outputPath = Path.of("${request.outputDirectory}", "$name.$group.fits")
            if (stacker.stack(map { it.path!! }, outputPath, request.referencePath!!)) outputPath else null
        } else if (isNotEmpty()) {
            val outputPath = Path.of("${request.outputDirectory}", "$name.$group.fits")
            if (stacker.align(request.referencePath!!, this[0].path!!, outputPath)) outputPath else null
        } else {
            null
        }
    }

    fun analyze(path: Path): AnalyzedTarget? {
        val image = if (path.isFits()) path.fits()
        else if (path.isXisf()) path.xisf()
        else return null

        return image.use { it.firstOrNull()?.header }?.let(::AnalyzedTarget)
    }
}
