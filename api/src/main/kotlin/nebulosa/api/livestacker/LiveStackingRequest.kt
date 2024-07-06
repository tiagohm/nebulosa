package nebulosa.api.livestacker

import nebulosa.livestacker.LiveStacker
import nebulosa.pixinsight.livestacker.PixInsightLiveStacker
import nebulosa.pixinsight.script.PixInsightIsRunning
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.pixinsight.script.PixInsightStartup
import nebulosa.siril.livestacker.SirilLiveStacker
import org.jetbrains.annotations.NotNull
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Supplier

data class LiveStackingRequest(
    @JvmField val enabled: Boolean = false,
    @JvmField val type: LiveStackerType = LiveStackerType.SIRIL,
    @JvmField @field:NotNull val executablePath: Path? = null,
    @JvmField val dark: Path? = null,
    @JvmField val flat: Path? = null,
    @JvmField val bias: Path? = null,
    @JvmField val use32Bits: Boolean = false,
    @JvmField val slot: Int = 1,
) : Supplier<LiveStacker> {

    override fun get(): LiveStacker {
        val workingDirectory = Files.createTempDirectory("ls-")

        return when (type) {
            LiveStackerType.SIRIL -> SirilLiveStacker(executablePath!!, workingDirectory, dark, flat, use32Bits)
            LiveStackerType.PIXINSIGHT -> {
                val runner = PixInsightScriptRunner(executablePath!!)

                if (!PixInsightIsRunning(slot).use { it.runSync(runner) }) {
                    if (!PixInsightStartup(slot).use { it.runSync(runner) }) {
                        throw IllegalStateException("unable to start PixInsight")
                    }
                }

                PixInsightLiveStacker(runner, workingDirectory, dark, flat, bias, use32Bits, slot)
            }
        }
    }

    companion object {

        @JvmStatic val DISABLED = LiveStackingRequest()
    }
}
