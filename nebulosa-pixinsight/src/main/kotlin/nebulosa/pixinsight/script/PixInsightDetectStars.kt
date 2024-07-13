package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import nebulosa.stardetector.StarPoint
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
) : AbstractPixInsightScript<PixInsightDetectStars.Output>() {

    private data class Input(
        @JvmField val targetPath: Path,
        @JvmField val statusPath: Path,
        @JvmField val minSNR: Double = 0.0,
        @JvmField val invert: Boolean = false,
    )

    data class Output(
        override val success: Boolean = false,
        override val errorMessage: String? = null,
        @JvmField val stars: List<Star> = emptyList(),
    ) : PixInsightOutput {

        override fun toString() = "Output(success=$success, errorMessage=$errorMessage, stars=${stars.size})"

        companion object {

            @JvmStatic val FAILED = Output()
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
    ) : StarPoint

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val statusPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/DetectStars.js")!!.transferAndClose(scriptPath.outputStream())
    }

    override val arguments = listOf("-x=${execute(slot, scriptPath, Input(targetPath, statusPath, minSNR, invert))}")

    override fun processOnComplete(exitCode: Int): Output {
        val timeoutInMillis = timeout.toMillis()

        if (exitCode == 0) {
            val startTime = System.currentTimeMillis()

            repeat(600) {
                val text = statusPath.readText()

                if (text.startsWith(START_FILE) && text.endsWith(END_FILE)) {
                    return OBJECT_MAPPER.readValue(text.substring(1, text.length - 1), Output::class.java)
                }

                if (timeoutInMillis == 0L || System.currentTimeMillis() - startTime < timeoutInMillis) {
                    Thread.sleep(500)
                } else {
                    return@repeat
                }
            }
        }

        return Output.FAILED
    }

    override fun close() {
        scriptPath.deleteIfExists()
        statusPath.deleteIfExists()
    }
}
