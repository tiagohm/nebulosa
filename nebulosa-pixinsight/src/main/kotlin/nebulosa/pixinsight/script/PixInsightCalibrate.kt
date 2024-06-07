package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.io.path.readText

data class PixInsightCalibrate(
    private val slot: Int,
    private val workingDirectory: Path,
    private val targetPath: Path,
    private val dark: Path? = null,
    private val flat: Path? = null,
    private val bias: Path? = null,
    private val compress: Boolean = false,
    private val use32Bit: Boolean = false,
) : AbstractPixInsightScript<PixInsightCalibrate.Output>() {

    private data class Input(
        @JvmField val targetPath: Path,
        @JvmField val outputDirectory: Path,
        @JvmField val statusPath: Path,
        @JvmField val masterDark: Path? = null,
        @JvmField val masterFlat: Path? = null,
        @JvmField val masterBias: Path? = null,
        @JvmField val compress: Boolean = false,
        @JvmField val use32Bit: Boolean = false,
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
        resource("pixinsight/Calibrate.js")!!.transferAndClose(scriptPath.outputStream())
    }

    override val arguments =
        listOf("-x=${execute(slot, scriptPath, Input(targetPath, workingDirectory, statusPath, dark, flat, bias, compress, use32Bit))}")

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
