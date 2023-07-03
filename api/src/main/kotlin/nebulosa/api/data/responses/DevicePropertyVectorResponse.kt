package nebulosa.api.data.responses

import nebulosa.api.data.enums.DevicePropertyVectorType
import nebulosa.indi.device.PropertyVector
import nebulosa.indi.device.SwitchPropertyVector
import nebulosa.indi.protocol.PropertyPermission
import nebulosa.indi.protocol.PropertyState
import nebulosa.indi.protocol.SwitchRule

data class DevicePropertyVectorResponse(
    val name: String,
    val label: String,
    val type: DevicePropertyVectorType,
    val group: String,
    val perm: PropertyPermission,
    val state: PropertyState,
    val rule: SwitchRule?,
    val properties: List<DevicePropertyResponse>,
) {

    constructor(vector: PropertyVector<*, *>) : this(
        vector.name,
        vector.label,
        DevicePropertyVectorType.of(vector),
        vector.group,
        vector.perm,
        vector.state,
        (vector as? SwitchPropertyVector)?.rule,
        vector.values.map(::DevicePropertyResponse),
    )
}
