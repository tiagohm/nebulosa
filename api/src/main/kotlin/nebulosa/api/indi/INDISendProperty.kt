package nebulosa.api.indi

import nebulosa.api.validators.Validatable
import nebulosa.api.validators.notBlank
import nebulosa.api.validators.notEmpty
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
