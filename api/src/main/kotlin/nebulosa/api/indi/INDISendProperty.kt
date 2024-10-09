package nebulosa.api.indi

import nebulosa.api.javalin.Validatable
import nebulosa.api.javalin.notBlank
import nebulosa.api.javalin.notEmpty
import nebulosa.indi.protocol.PropertyType

data class INDISendProperty(
    @JvmField val name: String = "",
    @JvmField val type: PropertyType = PropertyType.SWITCH,
    @JvmField val items: List<INDISendPropertyItem> = emptyList(),
) : Validatable {


    override fun validate() {
        name.notBlank()
        items.notEmpty().onEach { it.validate() }
    }
}
