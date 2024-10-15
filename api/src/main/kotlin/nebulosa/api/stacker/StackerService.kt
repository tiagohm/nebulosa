package nebulosa.api.stacker

import nebulosa.api.message.MessageService
import nebulosa.fits.fits
import nebulosa.fits.isFits
import nebulosa.image.format.ImageHdu
import nebulosa.stacker.AutoStacker
import nebulosa.stacker.AutoStackerListener
import nebulosa.xisf.isXisf
import nebulosa.xisf.xisf
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

class StackerService(private val messageService: MessageService?) {

    private val stackers = ConcurrentHashMap<String, AutoStacker>(1)

    fun stack(request: StackingRequest, key: String): Path? {
        require(request.outputDirectory != null && request.outputDirectory.exists() && request.outputDirectory.isDirectory())
        require(!stackers.containsKey(key)) { "stacking is already in progress" }

        val luminance = request.targets.filter { it.enabled && it.group == StackerGroupType.LUMINANCE }
        val red = request.targets.filter { it.enabled && it.group == StackerGroupType.RED }
        val green = request.targets.filter { it.enabled && it.group == StackerGroupType.GREEN }
        val blue = request.targets.filter { it.enabled && it.group == StackerGroupType.BLUE }
        val mono = request.targets.filter { it.enabled && it.group == StackerGroupType.MONO }
        val rgb = request.targets.filter { it.enabled && it.group == StackerGroupType.RGB }

        val name = "${System.currentTimeMillis()}"

        // Combined LRGB
        return if (red.size + green.size + blue.size >= 1) {
            val stacker = request.get()
            stackers[key] = stacker
            val autoStackerMessageHandler = AutoStackerMessageHandler(luminance, red, green, blue)

            try {
                stacker.registerAutoStackerListener(autoStackerMessageHandler)

                val stackedLuminancePath = luminance.stack(request, stacker, name, StackerGroupType.LUMINANCE)
                val stackedRedPath = red.stack(request, stacker, name, StackerGroupType.RED)
                val stackedGreenPath = green.stack(request, stacker, name, StackerGroupType.GREEN)
                val stackedBluePath = blue.stack(request, stacker, name, StackerGroupType.BLUE)

                val combinedPath = Path.of("${request.outputDirectory}", "$name.LRGB.fits")
                stacker.combineLRGB(combinedPath, stackedLuminancePath, stackedRedPath, stackedGreenPath, stackedBluePath)
                combinedPath
            } finally {
                messageService?.sendMessage(StackerEvent.IDLE)
                stacker.unregisterAutoStackerListener(autoStackerMessageHandler)
            }
        }
        // LRGB
        else if (rgb.isNotEmpty()) {
            val stacker = request.get()
            stackers[key] = stacker
            val autoStackerMessageHandler = AutoStackerMessageHandler(luminance, rgb = rgb)

            try {
                stacker.registerAutoStackerListener(autoStackerMessageHandler)

                val stackedLuminancePath = luminance.stack(request, stacker, name, StackerGroupType.LUMINANCE)
                val stackedRGBPath = rgb.stack(request, stacker, name, StackerGroupType.RGB)

                if (stackedLuminancePath != null && stackedRGBPath != null) {
                    val combinedPath = Path.of("${request.outputDirectory}", "$name.LRGB.fits")
                    stacker.combineLuminance(combinedPath, stackedLuminancePath, stackedRGBPath, false)
                    combinedPath
                } else {
                    stackedLuminancePath ?: stackedRGBPath
                }
            } finally {
                messageService?.sendMessage(StackerEvent.IDLE)
                stacker.unregisterAutoStackerListener(autoStackerMessageHandler)
            }
        }
        // MONO
        else if (mono.isNotEmpty() || luminance.isNotEmpty()) {
            val stacker = request.get()
            stackers[key] = stacker
            val autoStackerMessageHandler = AutoStackerMessageHandler(luminance, mono = mono)

            try {
                stacker.registerAutoStackerListener(autoStackerMessageHandler)

                val stackedLuminancePath = luminance.stack(request, stacker, name, StackerGroupType.LUMINANCE)
                val stackedMonoPath = mono.stack(request, stacker, name, StackerGroupType.MONO)

                if (stackedLuminancePath != null && stackedMonoPath != null) {
                    val combinedPath = Path.of("${request.outputDirectory}", "$name.LRGB.fits")
                    stacker.combineLuminance(combinedPath, stackedLuminancePath, stackedMonoPath, true)
                    combinedPath
                } else {
                    stackedLuminancePath ?: stackedMonoPath
                }
            } finally {
                messageService?.sendMessage(StackerEvent.IDLE)
                stacker.unregisterAutoStackerListener(autoStackerMessageHandler)
            }
        } else {
            null
        }
    }

    fun isRunning(key: String): Boolean {
        return stackers.containsKey(key)
    }

    fun stop(key: String) {
        stackers.remove(key)?.stop()
    }

    private fun List<StackingTarget>.stack(
        request: StackingRequest, stacker: AutoStacker,
        name: String, group: StackerGroupType
    ): Path? {
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

    fun analyze(path: Path): AnalyzedTarget? {
        if (!path.exists() || !path.isRegularFile()) return null

        val image = if (path.isFits()) path.fits()
        else if (path.isXisf()) path.xisf()
        else return null

        return image.use { it.firstOrNull { hdu -> hdu is ImageHdu }?.header }?.let(::AnalyzedTarget)
    }

    private inner class AutoStackerMessageHandler(
        private val luminance: Collection<StackingTarget> = emptyList(),
        private val red: Collection<StackingTarget> = emptyList(),
        private val green: Collection<StackingTarget> = emptyList(),
        private val blue: Collection<StackingTarget> = emptyList(),
        private val mono: Collection<StackingTarget> = emptyList(),
        private val rgb: Collection<StackingTarget> = emptyList(),
    ) : AutoStackerListener {

        private val numberOfTargets = luminance.size + red.size + green.size + blue.size + mono.size + rgb.size

        override fun onCalibrationStarted(stackCount: Int, path: Path) = sendNotification(StackerState.CALIBRATING, path, stackCount)

        override fun onAlignStarted(stackCount: Int, path: Path) = sendNotification(StackerState.ALIGNING, path, stackCount)

        override fun onIntegrationStarted(stackCount: Int, path: Path) = sendNotification(StackerState.INTEGRATING, path, stackCount)

        private fun sendNotification(state: StackerState, path: Path, stackCount: Int) {
            val type = if (luminance.any { it.path === path }) StackerGroupType.LUMINANCE
            else if (red.any { it.path === path }) StackerGroupType.RED
            else if (green.any { it.path === path }) StackerGroupType.GREEN
            else if (blue.any { it.path === path }) StackerGroupType.BLUE
            else if (mono.any { it.path === path }) StackerGroupType.MONO
            else if (rgb.any { it.path === path }) StackerGroupType.RGB
            else return

            messageService?.sendMessage(StackerEvent(state, type, stackCount + 1, numberOfTargets))
        }
    }
}
