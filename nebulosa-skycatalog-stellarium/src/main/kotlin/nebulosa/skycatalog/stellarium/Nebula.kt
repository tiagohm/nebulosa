package nebulosa.skycatalog.stellarium

import nebulosa.io.readDouble
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.mas
import nebulosa.math.Angle.Companion.rad
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.ICRF
import nebulosa.skycatalog.DSO
import nebulosa.skycatalog.SkyCatalog
import okio.BufferedSource
import okio.Source
import okio.buffer

class Nebula : SkyCatalog<DSO>() {

    fun load(
        source: Source,
        namesSource: Source? = null,
    ) {
        val buffer = source.buffer()

        buffer.readString() // Version.
        buffer.readString() // Edition.

        val namesMap = namesSource?.loadNames() ?: emptyMap()
        val types = NebulaType.values()

        while (!buffer.exhausted()) {
            val id = buffer.readInt()
            val ra = buffer.readDouble().rad
            val dec = buffer.readDouble().rad
            val mB = buffer.readDouble()
            val mV = buffer.readDouble()
            val type = (buffer.readInt() + 1) % 37
            val mType = buffer.readString()
            val majorAxis = buffer.readDouble().deg
            val minorAxis = buffer.readDouble().deg
            val orientation = buffer.readInt().deg
            val redshift = buffer.readDouble()
            buffer.readDouble() // redshiftError
            val parallax = buffer.readDouble().mas
            buffer.readDouble().mas // parallaxError
            val distance = buffer.readDouble()
            buffer.readDouble() // distanceError
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

            val names = ArrayList<String>(2)

            fun String.findNames(addKey: Boolean = true) {
                if (this in namesMap) names.addAll(namesMap[this]!!)
                if (addKey) names.add(this)
            }

            "$id".findNames(false)
            if (ngc > 0) "NGC $ngc".findNames()
            if (ic > 0) "IC $ic".findNames()
            if (m > 0) "M $m".findNames()
            if (mel > 0) "MEL $mel".findNames()
            if (b > 0) "B $b".findNames()
            if (c > 0) "C $c".findNames()
            if (cr > 0) "CR $cr".findNames()
            if (ced.isNotEmpty()) "CED $ced".findNames()
            if (sh2 > 0) "SH2 $sh2".findNames()
            if (rcw > 0) "RCW $rcw".findNames()
            if (vdb > 0) "VDB $vdb".findNames()
            if (lbn > 0) "LBN $lbn".findNames()
            if (pgc > 0) "PGC $pgc".findNames()
            if (ugc > 0) "UGC $ugc".findNames()
            if (arp > 0) "ARP $arp".findNames()
            if (vv > 0) "VV $vv".findNames()
            if (pk.isNotEmpty()) "PK $pk".findNames()
            if (png.isNotEmpty()) "PNG $png".findNames()
            if (aco.isNotEmpty()) "ACO $aco".findNames()
            if (eso.isNotEmpty()) "ESO $eso".findNames()
            if (snrg.isNotEmpty()) "SNRG $snrg".findNames()
            if (dwb > 0) "DWB $dwb".findNames()
            if (st > 0) "ST $st".findNames()
            if (ldn > 0) "LDN $ldn".findNames()
            if (hcg.isNotEmpty()) "HCG $hcg".findNames()
            if (vdbh.isNotEmpty()) "VDBH $vdbh".findNames()
            if (tr > 0) "TR $tr".findNames()
            if (ru > 0) "RU $ru".findNames()
            if (vdbha > 0) "VDBHA $vdbha".findNames()

            val nebula = DSO(
                id,
                names,
                m = m, ngc = ngc, ic = ic,
                c = c, b = b,
                sh2 = sh2, vdb = vdb,
                rcw = rcw, ldn = ldn, lbn = lbn,
                cr = cr, mel = mel,
                pgc = pgc, ugc = ugc,
                arp = arp, vv = vv,
                dwb = dwb,
                tr = tr, st = st, ru = ru,
                vdbha = vdbha, ced = ced,
                pk = pk, png = png,
                snrg = snrg, aco = aco,
                hcg = hcg, eso = eso, vdbh = vdbh,
                mB = mB, mV = mV,
                rightAscension = ra, declination = dec,
                type = types[type].type, mType = mType,
                majorAxis = majorAxis, minorAxis = minorAxis,
                orientation = orientation,
                redshift = redshift,
                parallax = parallax,
                distance = distance,
                constellation = computeConstellation(ra, dec),
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

        @JvmStatic
        private fun computeConstellation(rightAscension: Angle, declination: Angle): Constellation {
            return Constellation.find(ICRF.equatorial(rightAscension, declination))
        }
    }
}
