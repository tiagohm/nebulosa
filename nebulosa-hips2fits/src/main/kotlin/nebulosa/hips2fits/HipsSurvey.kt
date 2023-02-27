package nebulosa.hips2fits

import com.fasterxml.jackson.annotation.JsonProperty

data class HipsSurvey(
    @field:JsonProperty("ID") val id: String = "",
    @field:JsonProperty("client_category") val category: String = "",
    @field:JsonProperty("hips_copyright") val copyright: String = "",
    @field:JsonProperty("hips_frame") val frame: String = "",
    @field:JsonProperty("hips_pixel_bitpix") val bitPix: Int = 0,
    @field:JsonProperty("hips_pixel_scale") val pixelScale: Double = 0.0,
    @field:JsonProperty("obs_description") val description: List<String> = emptyList(),
    @field:JsonProperty("obs_title") val title: String = "",
    @field:JsonProperty("moc_sky_fraction") val skyFraction: Double = 0.0,
    @field:JsonProperty("obs_regime") val regime: String = "",
)
