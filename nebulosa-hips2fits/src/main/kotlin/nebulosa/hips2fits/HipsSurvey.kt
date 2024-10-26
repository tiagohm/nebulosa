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
) : Comparable<HipsSurvey> {

    override fun compareTo(other: HipsSurvey): Int {
        if (regime == other.regime) return -skyFraction.compareTo(other.skyFraction)
        return REGIME_SORT_ORDER.indexOf(regime).compareTo(REGIME_SORT_ORDER.indexOf(other.regime))
    }

    companion object {

        private val REGIME_SORT_ORDER = arrayOf("Optical", "Infrared", "UV", "Radio", "X-ray", "Gamma-ray")
    }
}
