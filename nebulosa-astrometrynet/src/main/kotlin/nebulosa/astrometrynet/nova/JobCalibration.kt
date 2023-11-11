package nebulosa.astrometrynet.nova

import com.fasterxml.jackson.annotation.JsonProperty

data class JobCalibration(
    @field:JsonProperty("parity") val parity: Parity = Parity.BOTH,
    @field:JsonProperty("orientation") val orientation: Double = 0.0,
    @field:JsonProperty("pixscale") val pixScale: Double = 0.0,
    @field:JsonProperty("radius") val radius: Double = 0.0,
    @field:JsonProperty("ra") val ra: Double = 0.0,
    @field:JsonProperty("dec") val dec: Double = 0.0,
    @field:JsonProperty("width_arcsec") val width: Double = 0.0,
    @field:JsonProperty("height_arcsec") val height: Double = 0.0,
)
