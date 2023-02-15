package nebulosa.query.simbad

import com.fasterxml.jackson.annotation.JsonProperty

data class Name(
    @field:JsonProperty("type") val type: CatalogType = CatalogType.NAME,
    @field:JsonProperty("name") val name: String = "",
)
