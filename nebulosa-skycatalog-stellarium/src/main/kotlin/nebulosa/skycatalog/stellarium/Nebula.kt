package nebulosa.skycatalog.stellarium

import nebulosa.io.readDouble
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.mas
import nebulosa.math.Angle.Companion.rad
import nebulosa.skycatalog.DeepSkyObject
import nebulosa.skycatalog.SkyCatalog
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObject.Companion.NAME_SEPARATOR
import nebulosa.time.UTC
import okio.BufferedSource
import okio.Source
import okio.buffer
import kotlin.math.min

class Nebula : SkyCatalog<DeepSkyObject>(94661) {

    fun load(
        source: Source,
        namesSource: Source? = null,
    ) {
        val buffer = source.buffer()

        buffer.readString() // Version.
        buffer.readString() // Edition.

        val currentTime = UTC.now()
        val namesMap = namesSource?.loadNames() ?: emptyMap()
        val names = ArrayList<String>(8)

        while (!buffer.exhausted()) {
            val id = buffer.readInt().toLong()
            val ra = buffer.readDouble().rad
            val dec = buffer.readDouble().rad
            val mB = buffer.readDouble()
            val mV = buffer.readDouble()
            val type = (buffer.readInt() + 1) % 37
            buffer.readString() // Morphological type
            val majorAxis = buffer.readDouble().deg
            val minorAxis = buffer.readDouble().deg
            val orientation = buffer.readInt().deg
            val redshift = buffer.readDouble()
            buffer.readDouble() // Redshift error
            val parallax = buffer.readDouble().mas
            buffer.readDouble().mas // Parallax error
            buffer.readDouble() // Distance
            buffer.readDouble() // Distance error
            val ngc = buffer.readInt()
            val ic = buffer.readInt()
            val m = buffer.readInt()
            val c = buffer.readInt()
            val b = buffer.readInt()
            val sh2 = buffer.readInt()
            val vdb = buffer.readInt()
            val rcw = buffer.readInt()
            val ldn = buffer.readInt()
            val lbn = buffer.readInt()
            val cr = buffer.readInt()
            val mel = buffer.readInt()
            val pgc = buffer.readInt()
            val ugc = buffer.readInt()
            val ced = buffer.readString()
            val arp = buffer.readInt()
            val vv = buffer.readInt()
            val pk = buffer.readString()
            val png = buffer.readString()
            val snrg = buffer.readString()
            val aco = buffer.readString()
            val hcg = buffer.readString()
            val eso = buffer.readString()
            val vdbh = buffer.readString()
            val dwb = buffer.readInt()
            val tr = buffer.readInt()
            val st = buffer.readInt()
            val ru = buffer.readInt()
            val vdbha = buffer.readInt()

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

            val nebula = DeepSkyObject(
                id,
                names.joinToString(NAME_SEPARATOR).trim(),
                min(mB, mV),
                ra, dec, NebulaType.entries[type].type,
                majorAxis, minorAxis, orientation,
                parallax, redshift = redshift,
                // distance * 3261.5637769,
                constellation = SkyObject.computeConstellation(ra, dec, currentTime),
            )

            add(nebula)
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
            val buffer = buffer()
            val res = HashMap<String, MutableList<String>>()

            while (!buffer.exhausted()) {
                val line = buffer.readUtf8Line() ?: break

                if (line.startsWith("#")) continue

                val name = DSO_NAME_REGEX.matchEntire(line.substring(20))?.groupValues?.get(1) ?: continue
                val prefix = line.substring(0..4).trim()
                val id = line.substring(5..19).trim()

                val key = if (prefix.isEmpty()) id else "$prefix $id"

                if (key !in res) res[key] = ArrayList(4)

                res[key]!!.add(name)
            }

            return res
        }
    }
}
