package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import nebulosa.star.detection.ImageStar
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.io.path.readText

data class PixInsightDetectStars(
    private val slot: Int,
    private val targetPath: Path,
    private val minSNR: Double = 0.0,
    private val invert: Boolean = false,
    private val timeout: Duration = Duration.ZERO,
) : AbstractPixInsightScript<PixInsightDetectStars.Result>() {

    @Suppress("ArrayInDataClass")
    data class Result(@JvmField val stars: Array<Star> = emptyArray()) {

        companion object {

            @JvmStatic val FAILED = Result()
        }
    }

    data class Star(
        override val x: Double = 0.0,
        override val y: Double = 0.0,
        override val flux: Double = 0.0,
        @JvmField val size: Double = 0.0,
        @JvmField val bkg: Double = 0.0,
        @JvmField val x0: Int = 0,
        @JvmField val y0: Int = 0,
        @JvmField val x1: Int = 0,
        @JvmField val y1: Int = 0,
        @JvmField val nmax: Int = 0,
        override val snr: Double = 0.0,
        @JvmField val peak: Double = 0.0,
        override val hfd: Double = 0.0,
    ) : ImageStar

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val outputPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/DetectStars.js")!!.transferAndClose(scriptPath.outputStream())
    }

    override val arguments = listOf("-x=${parameterize(slot, scriptPath, targetPath, outputPath, minSNR, invert)}")

    override fun processOnComplete(exitCode: Int): Result {
        val timeoutInMillis = timeout.toMillis()

        if (exitCode == 0) {
            val startTime = System.currentTimeMillis()

            repeat(600) {
                val text = outputPath.readText()

                if (text.startsWith(START_FILE) && text.endsWith(END_FILE)) {
                    return OBJECT_MAPPER.readValue(text.substring(1, text.length - 1), Result::class.java)
                }

                if (timeoutInMillis == 0L || System.currentTimeMillis() - startTime < timeoutInMillis) {
                    Thread.sleep(500)
                } else {
                    return@repeat
                }
            }
        }

        return Result.FAILED
    }

    override fun close() {
        scriptPath.deleteIfExists()
        outputPath.deleteIfExists()
    }
}
