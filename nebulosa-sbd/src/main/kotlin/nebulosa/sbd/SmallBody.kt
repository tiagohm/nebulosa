package nebulosa.sbd

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class SmallBody(
    @field:JsonProperty("orbit") val orbit: Orbit? = null,
    @field:JsonProperty("body") @field:JsonAlias("object") val body: Body? = null,
    @field:JsonProperty("physical") @field:JsonAlias("phys_par") val physical: List<PhysicalParameter>? = null,
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
        @field:JsonProperty("type") @field:JsonAlias("orbit_class") val type: OrbitType? = null,
        @field:JsonProperty("fullname") val fullname: String = "",
        @field:JsonProperty("des") val des: String = "",
        @field:JsonProperty("pha") val pha: Boolean = false,
        @field:JsonProperty("neo") val neo: Boolean = false,
        @field:JsonProperty("kind") val kind: BodyKind = BodyKind.ASTEROID,
        @field:JsonProperty("orbitId") @field:JsonAlias("orbit_id") val orbitId: Int = 0,
        @field:JsonProperty("prefix") val prefix: String? = null,
        @field:JsonProperty("spkId") @field:JsonAlias("spkid") val spkId: Int = 0,
        @field:JsonProperty("shortname") val shortname: String = "",
    )

    enum class BodyKind {
        @JsonAlias("an", "au")
        ASTEROID,

        @JsonAlias("cn", "cu")
        COMET
    }

    data class OrbitType(
        @field:JsonProperty("code") val code: String = "",
        @field:JsonProperty("name") val name: String = "",
    )

    data class PhysicalParameter(
        @field:JsonProperty("title") val title: String = "",
        @field:JsonProperty("sigma") val sigma: String? = null,
        @field:JsonProperty("units") val units: String? = null,
        @field:JsonProperty("name") val name: String = "",
        @field:JsonProperty("value") val value: String? = null,
        @field:JsonProperty("desc") val description: String = "",
        @field:JsonProperty("notes") val notes: String? = null,
    )

    data class SearchItem(
        @field:JsonProperty("name") val name: String = "",
        @field:JsonProperty("pdes") val pdes: String = "",
    )
}
