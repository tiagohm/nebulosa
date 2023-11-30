package nebulosa.skycatalog.stellarium

import nebulosa.io.readDouble
import nebulosa.math.deg
import nebulosa.math.mas
import nebulosa.math.rad
import nebulosa.skycatalog.SkyCatalog
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObject.Companion.NAME_SEPARATOR
import nebulosa.time.UTC
import okio.BufferedSource
import okio.Source
import okio.buffer
import kotlin.math.min

class Nebula : SkyCatalog<NebulaEntry>(94661) {

    fun load(
        source: Source,
        namesSource: Source? = null,
    ) {
        clear()

        (source as? BufferedSource ?: source.buffer()).use {
            it.readString() // Version.
            it.readString() // Edition.

            val currentTime = UTC.now()
            val namesMap = namesSource?.loadNames() ?: emptyMap()
            val names = ArrayList<String>(8)

            while (!it.exhausted()) {
                val id = it.readInt().toLong()
                val ra = it.readDouble().rad
                val dec = it.readDouble().rad
                val mB = it.readDouble()
                val mV = it.readDouble()
                val type = (it.readInt() + 1) % 37
                it.readString() // Morphological type
                val majorAxis = it.readDouble().deg
                val minorAxis = it.readDouble().deg
                val orientation = it.readInt().deg
                val redshift = it.readDouble()
                it.readDouble() // Redshift error
                val parallax = it.readDouble().mas
                it.readDouble().mas // Parallax error
                it.readDouble() // Distance
                it.readDouble() // Distance error
                val ngc = it.readInt()
                val ic = it.readInt()
                val m = it.readInt()
                val c = it.readInt()
                val b = it.readInt()
                val sh2 = it.readInt()
                val vdb = it.readInt()
                val rcw = it.readInt()
                val ldn = it.readInt()
                val lbn = it.readInt()
                val cr = it.readInt()
                val mel = it.readInt()
                val pgc = it.readInt()
                val ugc = it.readInt()
                val ced = it.readString()
                val arp = it.readInt()
                val vv = it.readInt()
                val pk = it.readString()
                val png = it.readString()
                val snrg = it.readString()
                val aco = it.readString()
                val hcg = it.readString()
                val eso = it.readString()
                val vdbh = it.readString()
                val dwb = it.readInt()
                val tr = it.readInt()
                val st = it.readInt()
                val ru = it.readInt()
                val vdbha = it.readInt()

                names.clear()

                fun String.findNames(useKeyAsName: Boolean = true) {
                    if (this in namesMap) names.addAll(namesMap[this]!!)
                    if (useKeyAsName) names.add(this)
                }

                "$id".findNames(false)
                if (ngc > 0) "NGC $ngc".findNames()
                if (ic > 0) "IC $ic".findNames()
                if (m > 0) "M $m".findNames()
                if (mel > 0) "Mellote $mel".findNames()
                if (b > 0) "Barnard $b".findNames()
                if (c > 0) "Caldwell $c".findNames()
                if (cr > 0) "Collinder $cr".findNames()
                if (ced.isNotEmpty()) "CED $ced".findNames()
                if (sh2 > 0) "SH 2-$sh2".findNames()
                if (rcw > 0) "RCW $rcw".findNames()
                if (vdb > 0) "VDB $vdb".findNames()
                if (lbn > 0) "LBN $lbn".findNames()
                if (pgc > 0) "PGC $pgc".findNames()
                if (ugc > 0) "UGC $ugc".findNames()
                if (arp > 0) "Arp $arp".findNames()
                if (vv > 0) "VV $vv".findNames()
                if (pk.isNotEmpty()) "PK $pk".findNames()
                if (png.isNotEmpty()) "PNG $png".findNames()
                if (aco.isNotEmpty()) "Abell $aco".findNames()
                if (eso.isNotEmpty()) "ESO $eso".findNames()
                if (snrg.isNotEmpty()) "SNRG $snrg".findNames()
                if (dwb > 0) "DWB $dwb".findNames()
                if (st > 0) "Stock $st".findNames()
                if (ldn > 0) "LDN $ldn".findNames()
                if (hcg.isNotEmpty()) "HCG $hcg".findNames()
                if (vdbh.isNotEmpty()) "VdBH $vdbh".findNames()
                if (tr > 0) "Trumpler $tr".findNames()
                if (ru > 0) "Ruprecht $ru".findNames()
                if (vdbha > 0) "VdBHA $vdbha".findNames()

                val nebula = NebulaEntry(
                    id,
                    names.joinToString(NAME_SEPARATOR).trim(),
                    ra, dec, min(mB, mV),
                    NebulaType.entries[type].type,
                    majorAxis, minorAxis, orientation,
                    parallax = parallax, redshift = redshift,
                    // distance * 3261.5637769,
                    constellation = SkyObject.computeConstellation(ra, dec, currentTime),
                )

                add(nebula)
            }
        }

        notifyLoadFinished()
    }

    companion object {

        @JvmStatic private val DSO_NAME_REGEX = Regex(".*_\\(\"(.*?)\"\\).*")

        @Suppress("NOTHING_TO_INLINE")
        private inline fun BufferedSource.readString(): String {
            return readString(readInt().toLong(), Charsets.UTF_16)
        }

        /**
         * Loads the Stellarium DSO Catalog Names file.
         */
        @JvmStatic
        private fun Source.loadNames(): Map<String, List<String>> {
            val res = HashMap<String, MutableList<String>>()

            (this as? BufferedSource ?: buffer()).use {
                while (!it.exhausted()) {
                    val line = it.readUtf8Line() ?: break

                    if (line.startsWith("#")) continue

                    val name = DSO_NAME_REGEX.matchEntire(line.substring(20))?.groupValues?.get(1) ?: continue
                    val prefix = line.substring(0..4).trim()
                    val id = line.substring(5..19).trim()

                    val key = if (prefix.isEmpty()) id else "$prefix $id"

                    if (key !in res) res[key] = ArrayList(4)

                    res[key]!!.add(name)
                }
            }

            return res
        }
    }
}
