package nebulosa.astrometrynet.nova

import com.fasterxml.jackson.annotation.JsonProperty

data class Upload(
    @field:JsonProperty("session") val session: String = "",
    @field:JsonProperty("url") val url: String? = null,
    @field:JsonProperty("allow_commercial_use") val allowCommercialUse: String = "d",
    @field:JsonProperty("allow_modifications") val allowModifications: String = "d",
    @field:JsonProperty("publicly_visible") val publiclyVisible: String = "n",
    @field:JsonProperty("scale_units") val scaleUnits: ScaleUnit = ScaleUnit.DEGREES_WIDTH,
    @field:JsonProperty("scale_lower") val scaleLower: Double = 0.1,
    @field:JsonProperty("scale_upper") val scaleUpper: Double = 180.0,
    @field:JsonProperty("scale_type") val scaleType: ScaleType = ScaleType.UPPER_LOWER,
    @field:JsonProperty("scale_est") val scaleEstimated: Double? = null,
    @field:JsonProperty("scale_err") val scaleError: Double? = null,
    @field:JsonProperty("center_ra") val centerRA: Double? = null,
    @field:JsonProperty("center_dec") val centerDEC: Double? = null,
    @field:JsonProperty("radius") val radius: Double? = null,
    @field:JsonProperty("downsample_factor") val downsampleFactor: Int = 0,
    @field:JsonProperty("tweak_order") val tweakOrder: Int = 2,
    @field:JsonProperty("crpix_center") val crpixCenter: Boolean = true,
    @field:JsonProperty("parity") val parity: Parity = Parity.BOTH,
)
