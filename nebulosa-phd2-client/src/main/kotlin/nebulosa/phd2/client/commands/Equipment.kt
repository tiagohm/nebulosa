package nebulosa.phd2.client.commands

import com.fasterxml.jackson.annotation.JsonProperty

data class Equipment(
    @field:JsonProperty("camera") val camera: EquipmentDevice? = null,
    @field:JsonProperty("mount") val mount: EquipmentDevice? = null,
    @field:JsonProperty("aux_mount") val auxMount: EquipmentDevice? = null,
    @field:JsonProperty("AO") val ao: EquipmentDevice? = null,
    @field:JsonProperty("rotator") val rotator: EquipmentDevice? = null,
)
