package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

data class PixInsightCalibrate(
    private val slot: Int,
    private val workingDirectory: Path,
    private val targetPath: Path,
    private val darkPath: Path? = null,
    private val flatPath: Path? = null,
    private val biasPath: Path? = null,
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
        override val success: Boolean = false,
        override val errorMessage: String? = null,
        @JvmField val outputImage: Path? = null,
    ) : PixInsightScript.Output {

        companion object {

            @JvmStatic val FAILED = Output()
        }
    }

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val statusPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/Calibrate.js")!!.transferAndClose(scriptPath.outputStream())
    }

    private val input = Input(targetPath, workingDirectory, statusPath, darkPath, flatPath, biasPath, compress, use32Bit)
    override val arguments = listOf("-x=${execute(slot, scriptPath, input)}")

    override fun processOnComplete(exitCode: Int): Output {
        if (exitCode == 0) {
            repeat(30) {
                statusPath.parseStatus<Output>()?.also { return it } ?: Thread.sleep(1000)
            }
        }

        return Output.FAILED
    }

    override fun close() {
        scriptPath.deleteIfExists()
        statusPath.deleteIfExists()
    }
}
