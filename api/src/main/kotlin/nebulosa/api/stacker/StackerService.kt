package nebulosa.api.stacker

import nebulosa.stacker.AutoStacker
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
class StackerService {

    fun stack(request: StackingRequest): Path? {
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

            val stackedLuminancePath = luminance.stack(request, stacker, name, StackerGroupType.LUMINANCE)
            val stackedRedPath = red.stack(request, stacker, name, StackerGroupType.RED)
            val stackedGreenPath = green.stack(request, stacker, name, StackerGroupType.GREEN)
            val stackedBluePath = blue.stack(request, stacker, name, StackerGroupType.BLUE)

            val combinedPath = Path.of("${request.outputDirectory}", "$name.LRGB.fits")
            stacker.combineLRGB(combinedPath, stackedLuminancePath, stackedRedPath, stackedGreenPath, stackedBluePath)
            combinedPath
        }
        // LRGB
        else if (rgb.size > 1 || luminance.size + rgb.size > 1) {
            val stacker = request.get()

            val stackedLuminancePath = luminance.stack(request, stacker, name, StackerGroupType.LUMINANCE)
            val stackedRGBPath = rgb.stack(request, stacker, name, StackerGroupType.RGB)

            if (stackedLuminancePath != null && stackedRGBPath != null) {
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

            val stackedLuminancePath = luminance.stack(request, stacker, name, StackerGroupType.LUMINANCE)
            val stackedMonoPath = mono.stack(request, stacker, name, StackerGroupType.MONO)

            if (stackedLuminancePath != null && stackedMonoPath != null) {
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

    private fun List<StackingTarget>.stack(request: StackingRequest, stacker: AutoStacker, name: String, group: StackerGroupType): Path? {
        return if (size > 1) {
            val outputPath = Path.of("${request.outputDirectory}", "$name.$group.fits")
            if (stacker.stack(map { it.path!! }, outputPath, request.referencePath!!)) outputPath else null
        } else if (isNotEmpty()) {
            val outputPath = Path.of("${request.outputDirectory}", "$name.$group.fits")
            if (stacker.align(request.referencePath!!, this[0].path!!, outputPath)) outputPath else null
        } else {
            null
        }
    }
}
