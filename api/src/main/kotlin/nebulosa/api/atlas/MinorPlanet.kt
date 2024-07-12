package nebulosa.api.atlas

import nebulosa.sbd.SmallBody

data class MinorPlanet(
    @JvmField val found: Boolean = false,
    @JvmField val name: String = "",
    @JvmField val spkId: Int = -1,
    @JvmField val kind: SmallBody.BodyKind? = null,
    @JvmField val pha: Boolean = false,
    @JvmField val neo: Boolean = false,
    @JvmField val orbitType: String = "",
    @JvmField val parameters: List<OrbitalPhysicalParameter> = emptyList(),
    @JvmField val searchItems: List<SearchItem> = emptyList(),
) {

    data class OrbitalPhysicalParameter(
        @JvmField val name: String,
        @JvmField val description: String,
        @JvmField val value: String,
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
        @JvmField val name: String,
        @JvmField val pdes: String,
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
