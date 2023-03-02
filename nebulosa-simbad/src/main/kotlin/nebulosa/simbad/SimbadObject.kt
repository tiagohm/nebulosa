package nebulosa.simbad

import com.fasterxml.jackson.annotation.JsonProperty

data class SimbadObject(
    @field:JsonProperty("id") val id: Long = 0,
    @field:JsonProperty("name") val name: String = "",
    @field:JsonProperty("type") val type: SimbadObjectType = SimbadObjectType.OBJECT_OF_UNKNOWN_NATURE,
    @field:JsonProperty("names") val names: List<Name> = emptyList(),
    @field:JsonProperty("ra") val ra: Double = Double.NaN,
    @field:JsonProperty("dec") val dec: Double = Double.NaN,
    @field:JsonProperty("pmRA") val pmRA: Double = Double.NaN,
    @field:JsonProperty("pmDEC") val pmDEC: Double = Double.NaN,
    @field:JsonProperty("plx") val plx: Double = Double.NaN,
    @field:JsonProperty("spType") val spType: String = "",
    @field:JsonProperty("morphType") val morphType: String = "",
    @field:JsonProperty("majorAxis") val majorAxis: Double = Double.NaN,
    @field:JsonProperty("minorAxis") val minorAxis: Double = Double.NaN,
    @field:JsonProperty("u") val u: Double = Double.NaN,
    @field:JsonProperty("b") val b: Double = Double.NaN,
    @field:JsonProperty("v") val v: Double = Double.NaN,
    @field:JsonProperty("r") val r: Double = Double.NaN,
    @field:JsonProperty("i") val i: Double = Double.NaN,
    @field:JsonProperty("j") val j: Double = Double.NaN,
    @field:JsonProperty("h") val h: Double = Double.NaN,
    @field:JsonProperty("k") val k: Double = Double.NaN,
    @field:JsonProperty("redshift") val redshift: Double = Double.NaN,
    @field:JsonProperty("rv") val rv: Double = Double.NaN,
)
