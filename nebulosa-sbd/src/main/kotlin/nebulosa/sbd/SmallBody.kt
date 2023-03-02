package nebulosa.sbd

import com.fasterxml.jackson.annotation.JsonProperty

data class SmallBody(
    @field:JsonProperty("orbit") val orbit: Orbit? = null,
    @field:JsonProperty("object") val body: Body? = null,
    @field:JsonProperty("phys_par") val physical: List<PhysicalParameter>? = null,
    @field:JsonProperty("list") val list: List<SearchItem>? = null,
    @field:JsonProperty("message") val message: String? = null,
) {

    data class Orbit(
        @field:JsonProperty("equinox") val equinox: String = "",
        @field:JsonProperty("epoch") val epoch: Double = 0.0,
        @field:JsonProperty("elements") val elements: List<OrbitElement> = emptyList(),
    )

    data class OrbitElement(
        @field:JsonProperty("sigma") val sigma: String? = null,
        @field:JsonProperty("title") val title: String = "",
        @field:JsonProperty("name") val name: String = "",
        @field:JsonProperty("units") val units: String? = null,
        @field:JsonProperty("value") val value: String? = null,
        @field:JsonProperty("label") val label: String = "",
    )

    data class Body(
        @field:JsonProperty("orbit_class") val type: BodyType = BodyType.EMPTY,
        @field:JsonProperty("fullname") val fullname: String = "",
        @field:JsonProperty("des") val des: String = "",
        @field:JsonProperty("pha") val pha: Boolean = false,
        @field:JsonProperty("neo") val neo: Boolean = false,
        @field:JsonProperty("kind") val kind: String = "",
        @field:JsonProperty("orbit_id") val orbitId: Int = 0,
        @field:JsonProperty("prefix") val prefix: String? = null,
        @field:JsonProperty("spkid") val spkId: Int = 0,
        @field:JsonProperty("shortname") val shortname: String = "",
    )

    data class BodyType(
        @field:JsonProperty("code") val code: String = "",
        @field:JsonProperty("name") val name: String = "",
    ) {

        companion object {

            @JvmStatic val EMPTY = BodyType()
        }
    }

    data class PhysicalParameter(
        @field:JsonProperty("title") val title: String = "",
        @field:JsonProperty("sigma") val sigma: String? = null,
        @field:JsonProperty("units") val units: String? = null,
        @field:JsonProperty("name") val name: String = "",
        @field:JsonProperty("value") val value: String = "",
        @field:JsonProperty("desc") val description: String = "",
        @field:JsonProperty("notes") val notes: String? = null,
    )

    data class SearchItem(
        @field:JsonProperty("name") val name: String = "",
        @field:JsonProperty("pdes") val pdes: String = "",
    )
}
