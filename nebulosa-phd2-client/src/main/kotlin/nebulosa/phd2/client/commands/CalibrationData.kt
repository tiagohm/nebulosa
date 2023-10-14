package nebulosa.phd2.client.commands

import com.fasterxml.jackson.annotation.JsonProperty

data class CalibrationData(
    @field:JsonProperty("calibrated") val calibrated: Boolean = false,
    @field:JsonProperty("xAngle") val xAngle: Double = 0.0,
    @field:JsonProperty("xRate") val xRate: Double = 0.0,
    @field:JsonProperty("xParity") val xParity: String = "+",
    @field:JsonProperty("yAngle") val yAngle: Double = 0.0,
    @field:JsonProperty("yRate") val yRate: Double = 0.0,
    @field:JsonProperty("yParity") val yParity: String = "+",
)
