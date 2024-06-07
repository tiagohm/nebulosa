package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.io.path.readText

data class PixInsightAlign(
    private val slot: Int,
    private val workingDirectory: Path,
    private val referencePath: Path,
    private val targetPath: Path,
) : AbstractPixInsightScript<PixInsightAlign.Output>() {

    private data class Input(
        @JvmField val referencePath: Path,
        @JvmField val targetPath: Path,
        @JvmField val outputDirectory: Path,
        @JvmField val statusPath: Path,
    )

    data class Output(
        @JvmField val success: Boolean = false,
        @JvmField val errorMessage: String? = null,
        @JvmField val outputImage: Path? = null,
        @JvmField val outputMaskImage: Path? = null,
        @JvmField val totalPairMatches: Int = 0,
        @JvmField val inliers: Int = 0,
        @JvmField val overlapping: Int = 0,
        @JvmField val regularity: Double = 0.0,
        @JvmField val quality: Double = 0.0,
        @JvmField val rmsError: Double = 0.0,
        @JvmField val rmsErrorDev: Double = 0.0,
        @JvmField val peakErrorX: Double = 0.0,
        @JvmField val peakErrorY: Double = 0.0,
        @JvmField val h11: Double = 0.0,
        @JvmField val h12: Double = 0.0,
        @JvmField val h13: Double = 0.0,
        @JvmField val h21: Double = 0.0,
        @JvmField val h22: Double = 0.0,
        @JvmField val h23: Double = 0.0,
        @JvmField val h31: Double = 0.0,
        @JvmField val h32: Double = 0.0,
        @JvmField val h33: Double = 0.0,
    ) {

        companion object {

            @JvmStatic val FAILED = Output()
        }
    }

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val statusPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/Align.js")!!.transferAndClose(scriptPath.outputStream())
    }

    override val arguments = listOf("-x=${execute(slot, scriptPath, Input(referencePath, targetPath, workingDirectory, statusPath))}")

    override fun processOnComplete(exitCode: Int): Output {
        if (exitCode == 0) {
            repeat(5) {
                val text = statusPath.readText()

                if (text.startsWith(START_FILE) && text.endsWith(END_FILE)) {
                    return OBJECT_MAPPER.readValue(text.substring(1, text.length - 1), Output::class.java)
                }

                Thread.sleep(1000)
            }
        }

        return Output.FAILED
    }

    override fun close() {
        scriptPath.deleteIfExists()
        statusPath.deleteIfExists()
    }
}
