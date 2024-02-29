package nebulosa.skycatalog.hyg

import de.siegmar.fastcsv.reader.CommentStrategy
import de.siegmar.fastcsv.reader.CsvReader
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.math.kms
import nebulosa.math.mas
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyCatalog
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObject.Companion.NAME_SEPARATOR
import java.io.InputStream
import java.io.InputStreamReader

/**
 * HYG star database archive.
 *
 * @see <a href="https://github.com/astronexus/HYG-Database">GitHub</a>
 */
class HygDatabase : SkyCatalog<HygEntry>(118005) {

    fun load(stream: InputStream) {
        clear()

        val reader = CSV_READER.ofNamedCsvRecord(InputStreamReader(stream, Charsets.UTF_8))

        val names = ArrayList<String>(7)

        for (record in reader) {
            val id = record.getField("id").toLong()
            if (id == 0L) continue
            val hip = record.getField("hip").takeIf { it.isNotEmpty() }?.toInt() ?: 0
            val hd = record.getField("hd").takeIf { it.isNotEmpty() }?.toInt() ?: 0
            val hr = record.getField("hr").takeIf { it.isNotEmpty() }?.toInt() ?: 0
            val rightAscension = record.getField("ra").toDouble().hours
            val declination = record.getField("dec").toDouble().deg
            // val name = record.getField("proper")
            // TODO: Distance, Parallax.
            // val distance = record.getField("dist").toDouble()
            val pmRA = record.getField("pmra").toDouble().mas
            val pmDEC = record.getField("pmdec").toDouble().mas
            val radialVelocity = record.getField("rv").toDouble().kms
            val magnitude = record.getField("mag").toDouble()
            val spType = record.getField("spect")
            val bayer = record.getField("bayer")
            val flamsteed = record.getField("flam").toIntOrNull() ?: 0
            val constellation = record.getField("con")
                .takeIf { it.isNotEmpty() }
                ?.uppercase()
                ?.let(Constellation::valueOf)
                ?: SkyObject.constellationFor(rightAscension, declination)

            names.clear()

            if (bayer.isNotEmpty()) names.add("$bayer ${constellation.iau}")
            if (flamsteed > 0) names.add("$flamsteed ${constellation.iau}")
            if (hip > 0) names.add("HIP $hip")
            if (hd > 0) names.add("HD $hd")
            if (hr > 0) names.add("HR $hr")

            if (names.isEmpty()) continue

            val star = HygEntry(
                id, names.joinToString(NAME_SEPARATOR).trim(),
                magnitude,
                rightAscension, declination,
                spType, pmRA, pmDEC,
                radialVelocity = radialVelocity,
                constellation = constellation,
            )

            add(star)
        }

        notifyLoadFinished()
    }

    companion object {

        @JvmStatic private val CSV_READER = CsvReader.builder()
            .fieldSeparator(',')
            .quoteCharacter('"')
            .commentCharacter('#')
            .commentStrategy(CommentStrategy.SKIP)
    }
}
