package nebulosa.api.indi

import nebulosa.api.javalin.Validatable
import nebulosa.api.javalin.notBlank

data class INDISendPropertyItem(
    @JvmField val name: String = "",
    @JvmField val value: Any = "",
) : Validatable {

    override fun validate() {
        name.notBlank()
    }
}
