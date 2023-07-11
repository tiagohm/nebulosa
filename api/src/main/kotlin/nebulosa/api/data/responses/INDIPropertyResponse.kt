package nebulosa.api.data.responses

import nebulosa.api.data.enums.INDISendPropertyType
import nebulosa.indi.device.PropertyVector
import nebulosa.indi.device.SwitchPropertyVector
import nebulosa.indi.protocol.PropertyPermission
import nebulosa.indi.protocol.PropertyState
import nebulosa.indi.protocol.SwitchRule

data class INDIPropertyResponse(
    val name: String,
    val label: String,
    val type: INDISendPropertyType,
    val group: String,
    val perm: PropertyPermission,
    val state: PropertyState,
    val rule: SwitchRule?,
    val items: List<INDIPropertyItemResponse>,
) {

    constructor(vector: PropertyVector<*, *>) : this(
        vector.name,
        vector.label,
        INDISendPropertyType.of(vector),
        vector.group,
        vector.perm,
        vector.state,
        (vector as? SwitchPropertyVector)?.rule,
        vector.values.map(::INDIPropertyItemResponse),
    )
}
