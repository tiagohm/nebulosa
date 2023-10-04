package nebulosa.phd2.client.commands

import com.fasterxml.jackson.annotation.JsonProperty

@Suppress("ArrayInDataClass")
data class LockShiftParams(
    @field:JsonProperty("enabled") val enabled: Boolean = false,
    @field:JsonProperty("rate") val rate: IntArray = IntArray(2),
    @field:JsonProperty("units") val units: RateUnit = RateUnit.ARCSEC_HOUR,
    @field:JsonProperty("axes") val axes: ShiftAxesType = ShiftAxesType.RADEC,
)
