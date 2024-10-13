package nebulosa.api.stacker

import nebulosa.api.validators.Validatable
import nebulosa.api.validators.exists
import nebulosa.api.validators.notNull
import java.nio.file.Path

data class StackingTarget(
    @JvmField val enabled: Boolean = true,
    @JvmField val path: Path? = null,
    @JvmField val group: StackerGroupType = StackerGroupType.MONO,
    @JvmField val debayer: Boolean = true,
) : Validatable {

    override fun validate() {
        path.notNull().exists()
    }
}
