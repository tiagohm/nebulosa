package nebulosa.query.simbad

import nebulosa.query.QueryService
import okhttp3.FormBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @see <a href="https://simbad.u-strasbg.fr/simbad/tap/tapsearch.html">Tables</a>
 * @see <a href="https://simbad.u-strasbg.fr/simbad/tap/help/adqlHelp.html">ADQL Cheat sheet</a>
 * @see <a href="http://simbad.u-strasbg.fr/guide/otypes.htx">Object types</a>
 */
class SimbadService(url: String = "https://simbad.u-strasbg.fr/") :
    QueryService(url, converterFactory = SimbadObjectConverterFactory), Simbad {

    private val service = retrofit.create(Simbad::class.java)

    override fun query(body: FormBody) = service.query(body)

    fun query(query: SimbadQuery): Call<List<SimbadObject>> {
        val body = FormBody.Builder()
            .add("request", "doQuery")
            .add("lang", "adql")
            .add("format", "tsv")
            .add("query", query.build())
            .build()

        return query(body)
    }

    private object SimbadObjectConverter : Converter<ResponseBody, List<SimbadObject>> {

        override fun convert(value: ResponseBody): List<SimbadObject> {
            val res = arrayListOf<SimbadObject>()

            value.use {
                val lines = it.byteStream().bufferedReader().lines()

                var first = true

                for (line in lines) {
                    if (line.isEmpty()) continue
                    else if (!first) res.add(line.parse() ?: continue)
                    else first = false
                }
            }

            return res
        }
    }

    private object SimbadObjectConverterFactory : Converter.Factory() {

        override fun responseBodyConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit,
        ): Converter<ResponseBody, *>? {
            if (type is ParameterizedType) {
                val args = type.actualTypeArguments

                when (type.rawType) {
                    List::class.java -> when (args[0]) {
                        SimbadObject::class.java -> return SimbadObjectConverter
                    }
                }
            }

            return null
        }
    }

    companion object {

        private const val OID = 0
        private const val MAIN_ID = 1
        private const val RA = 2
        private const val DEC = 3
        private const val PM_RA = 4
        private const val PM_DEC = 5
        private const val PLX = 6
        private const val OTYPE = 7
        private const val SPTYPE = 8
        private const val MTYPE = 9
        private const val MAJ_AXIS = 10
        private const val MIN_AXIS = 11
        private const val FLUX_U = 12
        private const val FLUX_B = 13
        private const val FLUX_V = 14
        private const val FLUX_R = 15
        private const val FLUX_I = 16
        private const val FLUX_J = 17
        private const val FLUX_H = 18
        private const val FLUX_K = 19
        private const val REDSHIFT = 20
        private const val RADVEL = 21
        private const val IDS = 22

        @JvmStatic
        private fun String.parse(): SimbadObject? {
            val parts = split("\t")

            require(parts.size == 23) { "invalid line: $this" }

            val names = parts[IDS].replace("\"", "").names().ifEmpty { return null }
            val id = parts[OID].toLong()
            val name = parts[MAIN_ID].replace("\"", "").trim()
            val ra = parts[RA].toDouble()
            val dec = parts[DEC].toDouble()
            val pmRA = parts[PM_RA].toDoubleOrNull() ?: 0.0
            val pmDEC = parts[PM_DEC].toDoubleOrNull() ?: 0.0
            val plx = parts[PLX].toDoubleOrNull() ?: 0.0
            val type = SimbadObjectType.of(parts[OTYPE].replace("\"", "").trim())!!
            val spType = parts[SPTYPE].replace("\"", "").trim()
            val mType = parts[MTYPE].replace("\"", "").trim()
            val majorAxis = parts[MAJ_AXIS].toDoubleOrNull() ?: 0.0
            val minorAxis = parts[MIN_AXIS].toDoubleOrNull() ?: 0.0
            val u = parts[FLUX_U].toDoubleOrNull() ?: Double.NaN
            val b = parts[FLUX_B].toDoubleOrNull() ?: Double.NaN
            val v = parts[FLUX_V].toDoubleOrNull() ?: Double.NaN
            val r = parts[FLUX_R].toDoubleOrNull() ?: Double.NaN
            val i = parts[FLUX_I].toDoubleOrNull() ?: Double.NaN
            val j = parts[FLUX_J].toDoubleOrNull() ?: Double.NaN
            val h = parts[FLUX_H].toDoubleOrNull() ?: Double.NaN
            val k = parts[FLUX_K].toDoubleOrNull() ?: Double.NaN
            val redshift = parts[REDSHIFT].toDoubleOrNull() ?: 0.0
            val rv = parts[RADVEL].toDoubleOrNull() ?: 0.0

            return SimbadObject(
                id, name, type,
                names,
                ra, dec, pmRA, pmDEC,
                plx, spType, mType,
                majorAxis, minorAxis,
                u, b, v, r, i, j, h, k,
                redshift, rv,
            )
        }

        @JvmStatic private val CATALOG_TYPES = CatalogType.values()

        @JvmStatic
        fun String.names(): List<Name> {
            val res = arrayListOf<Name>()

            for (p in split("|")) {
                a@ for (type in CATALOG_TYPES) {
                    if (p.startsWith(type.prefix)) {
                        res.add(Name(type, p.trim().substring(type.prefix.length).trim()))
                        break@a
                    }
                }
            }

            return res
        }
    }
}
