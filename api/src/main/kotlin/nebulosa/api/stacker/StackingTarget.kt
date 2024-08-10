package nebulosa.api.stacker

import jakarta.validation.constraints.NotNull
import java.nio.file.Path

data class StackingTarget(
    @JvmField val enabled: Boolean = true,
    @JvmField @field:NotNull val path: Path? = null,
    @JvmField val group: StackerGroupType = StackerGroupType.MONO,
    @JvmField val debayer: Boolean = true,
)
