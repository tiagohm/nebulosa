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
    private val path: Path,
    private val dark: Path? = null,
    private val flat: Path? = null,
    private val bias: Path? = null,
    private val compress: Boolean = false,
    private val use32Bit: Boolean = false,
) : AbstractPixInsightScript<Path?>() {

    private val outputDirectory = Files.createTempDirectory("pi-calibrate-")
    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val outputPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/Calibrate.js")!!.transferAndClose(scriptPath.outputStream())
    }

    override val arguments =
        listOf("-x=\"${if (slot > 0) "$slot:" else ""}$scriptPath,$path,$outputDirectory,$outputPath,$dark,$flat,$bias,$compress,$use32Bit\"")

    override fun processOnComplete(exitCode: Int): Path? {
        if (exitCode == 0) {
            repeat(5) {
                val text = outputPath.readText()

                if (text.startsWith(START_FILE) && text.endsWith(END_FILE)) {
                    return Path.of(text.substring(1, text.length - 1))
                }

                Thread.sleep(1000)
            }
        }

        return null
    }

    override fun close() {
        scriptPath.deleteIfExists()
        outputPath.deleteIfExists()
        outputDirectory.deleteRecursively()
    }

    companion object {

        private const val START_FILE = "@"
        private const val END_FILE = "#"
    }
}
