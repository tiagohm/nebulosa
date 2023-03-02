package nebulosa.desktop

import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.simbad.SimbadObject
import nebulosa.simbad.SimbadQuery
import nebulosa.simbad.SimbadService
import org.slf4j.LoggerFactory
import java.io.File

object NamedStarGenerator {

    @JvmStatic private val LOG = LoggerFactory.getLogger(NamedStarGenerator::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val simbad = SimbadService()
        val query = SimbadQuery()

        val namedStars = File("desktop/src/test/resources/IAU star names - Official IAU Catalog.csv")
            .readLines()
            .map { it.split(",") }

        val stars = ArrayList<SimbadObject>(namedStars.size)

        for (namedStar in namedStars) {
            val name = namedStar[0].trim()
            query.name("NAME $name")
            var star = simbad.query(query).execute().body()

            if (star.isNullOrEmpty()) {
                query.name(namedStar[1].trim())
                star = simbad.query(query).execute().body()
            }

            if (star.isNullOrEmpty()) LOG.warn("not found: $name")
            else if (star.size > 1) LOG.warn("multiple rows: $name")
            else {
                val magnitude = if (star[0].v.isFinite()) star[0].v
                else namedStar[6].toDoubleOrNull() ?: Double.NaN
                stars.add(star[0].copy(name = name, v = magnitude))
                LOG.info("found: $name")
            }
        }

        val file = File("desktop/src/main/resources/data/NAMED_STARS.json")
        ObjectMapper().writeValue(file, stars)
    }
}
