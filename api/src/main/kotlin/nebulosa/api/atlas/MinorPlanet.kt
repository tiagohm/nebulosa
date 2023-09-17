package nebulosa.api.atlas

import nebulosa.sbd.SmallBody

data class MinorPlanet(
    val found: Boolean = false,
    val name: String = "",
    val spkId: Int = -1,
    val kind: SmallBody.BodyKind? = null,
    val pha: Boolean = false, val neo: Boolean = false,
    val orbitType: String = "",
    val parameters: List<OrbitalPhysicalParameter> = emptyList(),
    val searchItems: List<SearchItem> = emptyList(),
) {

    data class OrbitalPhysicalParameter(
        val name: String,
        val description: String,
        val value: String,
    ) {

        constructor(param: SmallBody.OrbitElement) : this(
            param.name, param.title,
            param.value.orEmpty().plus(" ").plus(param.units.orEmpty()).trim()
        )

        constructor(param: SmallBody.PhysicalParameter) : this(
            param.name, param.title,
            param.value.orEmpty().plus(" ").plus(param.units.orEmpty()).trim()
        )
    }

    data class SearchItem(
        val name: String,
        val pdes: String,
    )

    companion object {

        @JvmStatic val EMPTY = MinorPlanet()

        @JvmStatic
        fun of(body: SmallBody): MinorPlanet {
            if (body.orbit != null) {
                val items = arrayListOf<OrbitalPhysicalParameter>()

                for (item in body.orbit!!.elements) {
                    items.add(OrbitalPhysicalParameter(item))
                }

                if (body.physical != null) {
                    for (item in body.physical!!) {
                        items.add(OrbitalPhysicalParameter(item))
                    }
                }

                return MinorPlanet(
                    true, body.body!!.fullname, body.body!!.spkId, body.body!!.kind,
                    body.body!!.pha, body.body!!.neo, body.body?.type?.name ?: "", items,
                )
            } else if (body.list != null) {
                val searchItems = body.list!!.map { SearchItem(it.name, it.pdes) }
                return MinorPlanet(searchItems = searchItems)
            } else {
                return EMPTY
            }
        }
    }
}
