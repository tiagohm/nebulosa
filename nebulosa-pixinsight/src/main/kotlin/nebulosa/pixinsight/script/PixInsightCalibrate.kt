package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.outputStream
import kotlin.io.path.readText

data class PixInsightCalibrate(
    private val slot: Int,
    private val targetPath: Path,
    private val dark: Path? = null,
    private val flat: Path? = null,
    private val bias: Path? = null,
    private val compress: Boolean = false,
    private val use32Bit: Boolean = false,
) : AbstractPixInsightScript<PixInsightCalibrate.Result>() {

    data class Result(
        @JvmField val outputImage: Path? = null,
    ) {

        companion object {

            @JvmStatic val FAILED = Result()
        }
    }

    private val outputDirectory = Files.createTempDirectory("pi-calibrate-")
    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val outputPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/Calibrate.js")!!.transferAndClose(scriptPath.outputStream())
    }

    override val arguments =
        listOf("-x=${parameterize(slot, scriptPath, targetPath, outputDirectory, outputPath, dark, flat, bias, compress, use32Bit)}")

    override fun processOnComplete(exitCode: Int): Result {
        if (exitCode == 0) {
            repeat(5) {
                val text = outputPath.readText()

                if (text.startsWith(START_FILE) && text.endsWith(END_FILE)) {
                    return OBJECT_MAPPER.readValue(text.substring(1, text.length - 1), Result::class.java)
                }

                Thread.sleep(1000)
            }
        }

        return Result.FAILED
    }

    override fun close() {
        scriptPath.deleteIfExists()
        outputPath.deleteIfExists()
        outputDirectory.deleteRecursively()
    }
}
