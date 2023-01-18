package nebulosa.alpaca.api

import com.fasterxml.jackson.annotation.JsonProperty

data class ConfiguredDevice(
    @JsonProperty("DeviceName") val name: String = "",
    @JsonProperty("DeviceType") val type: String = "",
    @JsonProperty("DeviceNumber") val number: Int = 0,
    @JsonProperty("UniqueID") val uid: String = "",
)
