package nebulosa.api.livestacker

import jakarta.validation.constraints.NotNull
import nebulosa.livestacker.LiveStacker
import nebulosa.pixinsight.livestacker.PixInsightLiveStacker
import nebulosa.pixinsight.script.startPixInsight
import nebulosa.siril.livestacker.SirilLiveStacker
import java.nio.file.Path

data class LiveStackingRequest(
    @JvmField val enabled: Boolean = false,
    @JvmField val type: LiveStackerType = LiveStackerType.SIRIL,
    @JvmField @field:NotNull val executablePath: Path? = null,
    @JvmField val darkPath: Path? = null,
    @JvmField val flatPath: Path? = null,
    @JvmField val biasPath: Path? = null,
    @JvmField val use32Bits: Boolean = false,
    @JvmField val slot: Int = 1,
    @JvmField val useCalibrationGroup: Boolean = false,
) {

    fun get(workingDirectory: Path): LiveStacker {
        return when (type) {
            LiveStackerType.SIRIL -> SirilLiveStacker(executablePath!!, workingDirectory, darkPath, flatPath, use32Bits)
            LiveStackerType.PIXINSIGHT -> {
                val runner = startPixInsight(executablePath!!, slot)
                PixInsightLiveStacker(runner, workingDirectory, darkPath, flatPath, biasPath, use32Bits, slot)
            }
        }
    }

    companion object {

        @JvmStatic val DISABLED = LiveStackingRequest()
    }
}
