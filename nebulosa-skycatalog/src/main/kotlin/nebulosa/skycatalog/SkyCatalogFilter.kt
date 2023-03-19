package nebulosa.skycatalog

import java.util.function.Predicate

data class SkyCatalogFilter(private val text: String) : Predicate<SkyObject> {

    override fun test(o: SkyObject): Boolean {
        return o.names.any { it.contains(text, true) }
    }
}
