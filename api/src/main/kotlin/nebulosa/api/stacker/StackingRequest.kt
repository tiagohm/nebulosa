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

data class StackingRequest(
    @JvmField @field:NotNull val outputDirectory: Path? = null,
    @JvmField val type: StackerType = StackerType.PIXINSIGHT,
    @JvmField @field:NotNull val executablePath: Path? = null,
    @JvmField val darkPath: Path? = null,
    @JvmField val flatPath: Path? = null,
    @JvmField val biasPath: Path? = null,
    @JvmField val use32Bits: Boolean = false,
    @JvmField val slot: Int = 1,
    @JvmField @field:NotNull val referencePath: Path? = null,
    @JvmField @field:Size(min = 2) @field:Valid val targets: List<StackingTarget> = emptyList(),
) : Supplier<AutoStacker> {

    override fun get(): AutoStacker {
        val workingDirectory = Files.createTempDirectory("as-")

        return when (type) {
            StackerType.PIXINSIGHT -> {
                val runner = startPixInsight(executablePath!!, slot)
                PixInsightAutoStacker(runner, workingDirectory, darkPath, flatPath, biasPath, slot)
            }
        }
    }
}
