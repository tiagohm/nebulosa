package nebulosa.api.data.responses

import nebulosa.indi.device.Property

data class INDIPropertyItemResponse(
    val name: String,
    val label: String,
    val value: Any?,
) {

    constructor(property: Property<*>) : this(
        property.name,
        property.label,
        property.value,
    )
}
