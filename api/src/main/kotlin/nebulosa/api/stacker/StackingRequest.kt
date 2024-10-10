package nebulosa.api.stacker

import nebulosa.api.javalin.Validatable
import nebulosa.api.javalin.minSize
import nebulosa.api.javalin.notNull
import nebulosa.pixinsight.script.startPixInsight
import nebulosa.pixinsight.stacker.PixInsightAutoStacker
import nebulosa.stacker.AutoStacker
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Supplier
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

data class StackingRequest(
    @JvmField val outputDirectory: Path? = null,
    @JvmField val type: StackerType = StackerType.PIXINSIGHT,
    @JvmField val executablePath: Path? = null,
    @JvmField val darkPath: Path? = null,
    @JvmField val darkEnabled: Boolean = false,
    @JvmField val flatPath: Path? = null,
    @JvmField val flatEnabled: Boolean = false,
    @JvmField val biasPath: Path? = null,
    @JvmField val biasEnabled: Boolean = false,
    @JvmField val use32Bits: Boolean = false,
    @JvmField val slot: Int = 1,
    @JvmField val referencePath: Path? = null,
    @JvmField val targets: List<StackingTarget> = emptyList(),
) : Supplier<AutoStacker>, Validatable {

    override fun validate() {
        outputDirectory.notNull().exists()
        executablePath.notNull()
        referencePath.notNull().exists()
        targets.minSize(2).onEach { it.validate() }
    }

    override fun get(): AutoStacker {
        val workingDirectory = Files.createTempDirectory("as-")

        val darkPath = darkPath?.takeIf { darkEnabled && it.exists() && it.isRegularFile() }
        val flatPath = flatPath?.takeIf { flatEnabled && it.exists() && it.isRegularFile() }
        val biasPath = biasPath?.takeIf { biasEnabled && it.exists() && it.isRegularFile() }

        return when (type) {
            StackerType.PIXINSIGHT -> {
                val runner = startPixInsight(executablePath!!, slot)
                PixInsightAutoStacker(runner, workingDirectory, darkPath, flatPath, biasPath, slot)
            }
        }
    }
}
