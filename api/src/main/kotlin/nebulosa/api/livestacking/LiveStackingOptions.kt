package nebulosa.api.livestacking

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import nebulosa.api.beans.converters.angle.DegreesDeserializer
import nebulosa.livestacking.LiveStacker
import nebulosa.siril.livestacking.SirilLiveStacker
import org.jetbrains.annotations.NotNull
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Supplier

data class LiveStackingOptions(
    @JvmField val type: LiveStackerType = LiveStackerType.SIRIL,
    @JvmField @field:NotNull val executablePath: Path? = null,
    @JvmField val dark: Path? = null,
    @JvmField val flat: Path? = null,
    @JvmField @field:JsonDeserialize(using = DegreesDeserializer::class) val rotate: Double = 0.0,
    @JvmField val use32Bits: Boolean = false,
) : Supplier<LiveStacker> {

    override fun get(): LiveStacker {
        val workingDirectory = Files.createTempDirectory("ls-")

        return when (type) {
            LiveStackerType.SIRIL -> SirilLiveStacker(executablePath!!, workingDirectory, dark, flat, rotate, use32Bits)
        }
    }
}
