package nebulosa.api.stacker

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import nebulosa.pixinsight.script.startPixInsight
import nebulosa.pixinsight.stacker.PixInsightAutoStacker
import nebulosa.stacker.AutoStacker
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Supplier
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

data class StackingRequest(
    @JvmField @field:NotNull val outputDirectory: Path? = null,
    @JvmField val type: StackerType = StackerType.PIXINSIGHT,
    @JvmField @field:NotNull val executablePath: Path? = null,
    @JvmField val darkPath: Path? = null,
    @JvmField val darkEnabled: Boolean = false,
    @JvmField val flatPath: Path? = null,
    @JvmField val flatEnabled: Boolean = false,
    @JvmField val biasPath: Path? = null,
    @JvmField val biasEnabled: Boolean = false,
    @JvmField val use32Bits: Boolean = false,
    @JvmField val slot: Int = 1,
    @JvmField @field:NotNull val referencePath: Path? = null,
    @JvmField @field:Size(min = 2) @field:Valid val targets: List<StackingTarget> = emptyList(),
) : Supplier<AutoStacker> {

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
