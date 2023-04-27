package nebulosa.simbad

import com.fasterxml.jackson.annotation.JsonProperty
import nebulosa.skycatalog.SkyObjectType
import kotlin.math.min

data class SimbadObject(
    @field:JsonProperty("id") val id: Long = 0,
    @field:JsonProperty("name") val name: String = "",
    @field:JsonProperty("type") val type: SkyObjectType = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE,
    @field:JsonProperty("names") val names: List<Name> = emptyList(),
    @field:JsonProperty("ra") val ra: Double = 0.0,
    @field:JsonProperty("dec") val dec: Double = 0.0,
    @field:JsonProperty("pmRA") val pmRA: Double = 0.0,
    @field:JsonProperty("pmDEC") val pmDEC: Double = 0.0,
    @field:JsonProperty("plx") val plx: Double = 0.0,
    @field:JsonProperty("spType") val spType: String = "",
    @field:JsonProperty("morphType") val morphType: String = "",
    @field:JsonProperty("majorAxis") val majorAxis: Double = 0.0,
    @field:JsonProperty("minorAxis") val minorAxis: Double = 0.0,
    @field:JsonProperty("u") val u: Double = 99.0,
    @field:JsonProperty("b") val b: Double = 99.0,
    @field:JsonProperty("v") val v: Double = 99.0,
    @field:JsonProperty("r") val r: Double = 99.0,
    @field:JsonProperty("i") val i: Double = 99.0,
    @field:JsonProperty("j") val j: Double = 99.0,
    @field:JsonProperty("h") val h: Double = 99.0,
    @field:JsonProperty("k") val k: Double = 99.0,
    @field:JsonProperty("redshift") val redshift: Double = 0.0,
    @field:JsonProperty("rv") val rv: Double = 0.0,
) {

    val magnitude
        get() = min(v, b).let { if (it < 99.0) it else min(u, min(r, min(i, min(j, min(h, k))))) }
}
