package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.io.path.readText

data class PixInsightPixelMath(
    private val slot: Int,
    private val inputPaths: List<Path>,
    private val outputPath: Path,
    private val expressionRK: String? = null,
    private val expressionG: String? = null,
    private val expressionB: String? = null,
) : AbstractPixInsightScript<PixInsightPixelMath.Output>() {

    private data class Input(
        @JvmField val statusPath: Path,
        @JvmField val inputPaths: List<Path>,
        @JvmField val outputPath: Path,
        @JvmField val expressionRK: String? = null,
        @JvmField val expressionG: String? = null,
        @JvmField val expressionB: String? = null,
    )

    data class Output(
        override val success: Boolean = false,
        override val errorMessage: String? = null,
        @JvmField val outputImage: Path? = null,
    ) : PixInsightOutput {

        companion object {

            @JvmStatic val FAILED = Output()
        }
    }

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val statusPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/PixelMath.js")!!.transferAndClose(scriptPath.outputStream())
    }

    override val arguments =
        listOf("-x=${execute(slot, scriptPath, Input(statusPath, inputPaths, outputPath, expressionRK, expressionG, expressionB))}")

    override fun processOnComplete(exitCode: Int): Output? {
        if (exitCode == 0) {
            repeat(30) {
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
