package nebulosa.skycatalog.stellarium

import nebulosa.io.readDouble
import nebulosa.math.deg
import nebulosa.math.mas
import nebulosa.math.rad
import nebulosa.skycatalog.SkyCatalog
import nebulosa.skycatalog.SkyObject
import okio.BufferedSource
import okio.Source
import okio.buffer
import kotlin.math.min

class Nebula : SkyCatalog<NebulaEntry>(94661) {

    fun load(
        catalog: Source,
        namesSource: Source? = null,
    ) {
        clear()

        (catalog as? BufferedSource ?: catalog.buffer()).use { source ->
            source.readString() // Version.
            source.readString() // Edition.

            val commonNames = namesSource?.let(::namesFor) ?: emptyList()
            val names = ArrayList<String>(8)

            while (!source.exhausted()) {
                val id = source.readInt().toLong()
                val ra = source.readDouble().rad
                val dec = source.readDouble().rad
                val mB = source.readDouble()
                val mV = source.readDouble()
                val type = (source.readInt() + 1) % 37
                source.readString() // Morphological type
                val majorAxis = source.readDouble().deg
                val minorAxis = source.readDouble().deg
                val orientation = source.readInt().deg
                val redshift = source.readDouble()
                source.readDouble() // Redshift error
                val parallax = source.readDouble().mas
                source.readDouble().mas // Parallax error
                source.readDouble() // Distance
                source.readDouble() // Distance error
                val ngc = source.readInt()
                val ic = source.readInt()
                val m = source.readInt()
                val c = source.readInt()
                val b = source.readInt()
                val sh2 = source.readInt()
                val vdb = source.readInt()
                val rcw = source.readInt()
                val ldn = source.readInt()
                val lbn = source.readInt()
                val cr = source.readInt()
                val mel = source.readInt()
                val pgc = source.readInt()
                val ugc = source.readInt()
                val ced = source.readString()
                val arp = source.readInt()
                val vv = source.readInt()
                val pk = source.readString()
                val png = source.readString()
                val snrg = source.readString()
                val aco = source.readString()
                val hcg = source.readString()
                val eso = source.readString()
                val vdbh = source.readString()
                val dwb = source.readInt()
                val tr = source.readInt()
                val st = source.readInt()
                val ru = source.readInt()
                val vdbha = source.readInt()

                names.clear()

                fun String.findNames(useKeyAsName: Boolean = true) {
                    commonNames.asSequence().filter { it.id == this }.forEach { names.add(it.name) }
                    if (useKeyAsName) names.add(this)
                }

                "$id".findNames(false)
                if (ngc > 0) "NGC $ngc".findNames()
                if (ic > 0) "IC $ic".findNames()
                if (m > 0) "M $m".findNames()
                if (mel > 0) "Mel $mel".findNames()
                if (b > 0) "B $b".findNames()
                if (c > 0) "C $c".findNames()
                if (cr > 0) "Cr $cr".findNames()
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
                if (st > 0) "St $st".findNames()
                if (ldn > 0) "LDN $ldn".findNames()
                if (hcg.isNotEmpty()) "HCG $hcg".findNames()
                if (vdbh.isNotEmpty()) "VdBH $vdbh".findNames()
                if (tr > 0) "Tr $tr".findNames()
                if (ru > 0) "Ru $ru".findNames()
                if (vdbha > 0) "VdBHA $vdbha".findNames()

                val nebula = NebulaEntry(
                    id, names,
                    ra, dec, min(mB, mV),
                    NebulaType.entries[type].type,
                    majorAxis, minorAxis, orientation,
                    parallax = parallax, redshift = redshift,
                    // distance * 3261.5637769,
                    constellation = SkyObject.constellationFor(ra, dec),
                )

                add(nebula)
            }
        }

        notifyLoadFinished()
    }

    companion object {

        private val DSO_NAME_REGEX = Regex("_\\(\"(.*?)\"\\)")

        @Suppress("NOTHING_TO_INLINE")
        private inline fun BufferedSource.readString(): String {
            return readString(readInt().toLong(), Charsets.UTF_16)
        }

        /**
         * Loads the Stellarium DSO Catalog Names file.
         */
        @JvmStatic
        fun namesFor(source: Source): List<Name> {
            val names = ArrayList<Name>()

            (source as? BufferedSource ?: source.buffer()).use {
                while (!it.exhausted()) {
                    val line = it.readUtf8Line() ?: break

                    if (line.startsWith("#") || line.isBlank()) continue

                    val prefix = prefixFor(line.substring(0..4).trim())
                    val id = line.substring(5..19).trim()
                    val name = DSO_NAME_REGEX.find(line.substring(20))?.groupValues?.get(1) ?: continue
                    val key = if (prefix.isEmpty()) id else "$prefix$id"
                    names.add(Name(key, name))
                }
            }

            return names
        }

        @Suppress("NOTHING_TO_INLINE")
        private inline fun prefixFor(prefix: String) = when (prefix) {
            "SH2" -> "SH 2-"
            "SNRG" -> "SNR G"
            else -> "$prefix "
        }
    }
}
