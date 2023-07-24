package nebulosa.hips2fits

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class HipsSurvey(
    @field:JsonProperty("id") @field:JsonAlias("ID") val id: String = "",
    // @field:JsonProperty("title") @field:JsonAlias("obs_title") val title: String = "",
    @field:JsonProperty("category") @field:JsonAlias("client_category") val category: String = "",
    // @field:JsonProperty("copyright")  @field:JsonAlias("hips_copyright") val copyright: String = "",
    @field:JsonProperty("frame") @field:JsonAlias("hips_frame") val frame: String = "",
    @field:JsonProperty("regime") @field:JsonAlias("obs_regime") val regime: String = "",
    @field:JsonProperty("bitpix") @field:JsonAlias("hips_pixel_bitpix") val bitPix: Int = 0,
    @field:JsonProperty("pixel_scale") @field:JsonAlias("hips_pixel_scale") val pixelScale: Double = 0.0,
    @field:JsonProperty("sky_fraction") @field:JsonAlias("moc_sky_fraction") val skyFraction: Double = 0.0,
)
