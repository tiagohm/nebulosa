package nebulosa.api.data.responses

import nebulosa.sbd.SmallBody

data class MinorPlanetResponse(
    val found: Boolean = false,
    val name: String = "", val spkId: Int = -1,
    val kind: String = "",
    val pha: Boolean = false, val neo: Boolean = false,
    val orbitType: String = "",
    val items: List<OrbitalPhysicalItem> = emptyList(),
    val searchItems: List<SearchItem> = emptyList(),
) {

    data class OrbitalPhysicalItem(
        val orbital: Boolean,
        val name: String,
        val description: String,
        val value: String,
        val unit: String,
    )

    data class SearchItem(
        val name: String,
        val pdes: String,
    )

    companion object {

        @JvmStatic val EMPTY = MinorPlanetResponse()

        @JvmStatic
        fun of(body: SmallBody): MinorPlanetResponse {
            if (body.orbit != null) {
                val items = arrayListOf<OrbitalPhysicalItem>()

                for (item in body.orbit!!.elements) {
                    items.add(OrbitalPhysicalItem(true, item.name, item.title, item.value ?: "", item.units ?: ""))
                }

                if (body.physical != null) {
                    for (item in body.physical!!) {
                        items.add(OrbitalPhysicalItem(false, item.name, item.title, item.value ?: "", item.units ?: ""))
                    }
                }

                return MinorPlanetResponse(
                    true, body.body!!.fullname, body.body!!.spkId, body.body!!.kind,
                    body.body!!.pha, body.body!!.neo, body.body?.type?.code ?: "", items,
                )
            } else if (body.list != null) {
                val searchItems = body.list!!.map { SearchItem(it.name, it.pdes) }
                return MinorPlanetResponse(searchItems = searchItems)
            } else {
                return EMPTY
            }
        }
    }
}
