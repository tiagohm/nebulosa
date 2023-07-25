package nebulosa.hips2fits

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class HipsSurvey(
    @field:JsonProperty("id") @field:JsonAlias("ID") val id: String = "",
    @field:JsonProperty("category") @field:JsonAlias("client_category") val category: String = "",
    @field:JsonProperty("frame") @field:JsonAlias("hips_frame") val frame: String = "",
    @field:JsonProperty("regime") @field:JsonAlias("obs_regime") val regime: String = "",
    @field:JsonProperty("bitPix") @field:JsonAlias("hips_pixel_bitpix") val bitPix: Int = 0,
    @field:JsonProperty("pixelScale") @field:JsonAlias("hips_pixel_scale") val pixelScale: Double = 0.0,
    @field:JsonProperty("skyFraction") @field:JsonAlias("moc_sky_fraction") val skyFraction: Double = 0.0,
)
