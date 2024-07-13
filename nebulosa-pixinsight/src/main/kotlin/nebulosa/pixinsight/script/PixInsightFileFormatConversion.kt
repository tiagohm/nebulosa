package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.io.path.readText

data class PixInsightFileFormatConversion(
    private val slot: Int,
    private val inputPath: Path,
    private val outputPath: Path,
) : AbstractPixInsightScript<PixInsightFileFormatConversion.Output>() {

    private data class Input(
        @JvmField val inputPath: Path,
        @JvmField val outputPath: Path,
        @JvmField val statusPath: Path,
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
        resource("pixinsight/FileFormatConversion.js")!!.transferAndClose(scriptPath.outputStream())
    }

    override val arguments =
        listOf("-x=${execute(slot, scriptPath, Input(inputPath, outputPath, statusPath))}")

    override fun processOnComplete(exitCode: Int): Output {
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
