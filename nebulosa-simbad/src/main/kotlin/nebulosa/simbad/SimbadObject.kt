package nebulosa.simbad

import com.fasterxml.jackson.annotation.JsonProperty
import nebulosa.skycatalog.SkyObject
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
    @field:JsonProperty("u") val u: Double = SkyObject.UNKNOWN_MAGNITUDE,
    @field:JsonProperty("b") val b: Double = SkyObject.UNKNOWN_MAGNITUDE,
    @field:JsonProperty("v") val v: Double = SkyObject.UNKNOWN_MAGNITUDE,
    @field:JsonProperty("r") val r: Double = SkyObject.UNKNOWN_MAGNITUDE,
    @field:JsonProperty("i") val i: Double = SkyObject.UNKNOWN_MAGNITUDE,
    @field:JsonProperty("j") val j: Double = SkyObject.UNKNOWN_MAGNITUDE,
    @field:JsonProperty("h") val h: Double = SkyObject.UNKNOWN_MAGNITUDE,
    @field:JsonProperty("k") val k: Double = SkyObject.UNKNOWN_MAGNITUDE,
    @field:JsonProperty("redshift") val redshift: Double = 0.0,
    @field:JsonProperty("rv") val rv: Double = 0.0,
) {

    val magnitude
        get() = min(v, b).let { if (it < SkyObject.UNKNOWN_MAGNITUDE) it else min(u, min(r, min(i, min(j, min(h, k))))) }
}
