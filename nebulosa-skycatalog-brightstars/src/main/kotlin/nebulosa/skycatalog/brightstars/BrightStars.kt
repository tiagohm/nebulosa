package nebulosa.skycatalog.brightstars

import nebulosa.io.resource
import nebulosa.skycatalog.SkyCatalog
import nebulosa.skycatalog.Star
import java.io.ObjectInputStream

@Suppress("UNCHECKED_CAST")
object BrightStars : SkyCatalog<Star>() {

    fun load() {
        val inputStream = resource("BrightStars.dat")!!
        val ois = ObjectInputStream(inputStream)
        val catalog = ois.use { ois.readObject() } as List<Star>
        addAll(catalog)
    }
}
