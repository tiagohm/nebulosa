package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.io.path.readText

data class PixInsightLRGBCombination(
    private val slot: Int,
    private val outputPath: Path,
    private val luminancePath: Path? = null,
    private val redPath: Path? = null,
    private val greenPath: Path? = null,
    private val bluePath: Path? = null,
) : AbstractPixInsightScript<PixInsightLRGBCombination.Output>() {

    @Suppress("ArrayInDataClass")
    private data class Input(
        @JvmField val outputPath: Path,
        @JvmField val statusPath: Path,
        @JvmField val luminancePath: Path?,
        @JvmField val redPath: Path?,
        @JvmField val greenPath: Path?,
        @JvmField val bluePath: Path?,
        @JvmField val channelWeights: DoubleArray,
    )

    data class Output(
        @JvmField val success: Boolean = false,
        @JvmField val errorMessage: String? = null,
        @JvmField val outputImage: Path? = null,
    ) {

        companion object {

            @JvmStatic val FAILED = Output()
        }
    }

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val statusPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/LRGBCombination.js")!!.transferAndClose(scriptPath.outputStream())
    }

    override val arguments =
        listOf("-x=${execute(slot, scriptPath, Input(outputPath, statusPath, luminancePath, redPath, greenPath, bluePath, DEFAULT_CHANNEL_WEIGHTS))}")

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

    companion object {

        @JvmStatic private val DEFAULT_CHANNEL_WEIGHTS = doubleArrayOf(1.0, 1.0, 1.0, 1.0)
    }
}
