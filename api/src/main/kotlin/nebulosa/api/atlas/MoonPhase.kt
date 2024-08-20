package nebulosa.api.atlas

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class MoonPhase(
    @field:JsonProperty("phase") @JvmField val phase: Double = 0.0,
    @field:JsonProperty("obscuration") @JvmField val obscuration: Double = 0.0,
    @field:JsonProperty("age") @JvmField val age: Double = 0.0,
    @field:JsonProperty("diameter") @JvmField val diameter: Double = 0.0,
    @field:JsonProperty("distance") @JvmField val distance: Double = 0.0,
    @field:JsonAlias("subsolar_lon") @JvmField val subSolarLon: Double = 0.0,
    @field:JsonAlias("subsolar_lat") @JvmField val subSolarLat: Double = 0.0,
    @field:JsonAlias("subearth_lon") @JvmField val subEarthLon: Double = 0.0,
    @field:JsonAlias("subearth_lat") @JvmField val subEarthLat: Double = 0.0,
    @field:JsonAlias("posangle") @JvmField val posAngle: Double = 0.0,
) {

    companion object {

        @JvmStatic val EMPTY = MoonPhase()
    }
}
