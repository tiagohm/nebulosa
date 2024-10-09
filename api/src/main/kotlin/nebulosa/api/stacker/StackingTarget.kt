package nebulosa.api.stacker

import nebulosa.api.javalin.Validatable
import nebulosa.api.javalin.exists
import nebulosa.api.javalin.notNull
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
