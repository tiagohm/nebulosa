package nebulosa.api.indi

import nebulosa.api.validators.Validatable
import nebulosa.api.validators.notBlank

data class INDISendPropertyItem(
    @JvmField val name: String = "",
    @JvmField val value: Any = "",
) : Validatable {

    override fun validate() {
        name.notBlank()
    }
}
