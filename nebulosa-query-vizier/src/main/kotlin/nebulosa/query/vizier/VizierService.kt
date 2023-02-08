package nebulosa.query.vizier

import nebulosa.query.QueryService
import okhttp3.FormBody

/**
 * @see <a href="http://cdsarc.u-strasbg.fr/doc/asu-summary.htx">Documentation</a>
 */
class VizierService(url: String = "http://vizier.cfa.harvard.edu") :
    QueryService(url), Vizier {

    private val service = retrofit.create(Vizier::class.java)

    override fun query(body: FormBody) = service.query(body)

    /**
     * Search Vizier for catalogs based on a set of keywords.
     */
    fun catalogs(
        vararg keywords: String,
        ucd: String = "",
    ) {
        val body = FormBody.Builder()
            .add("-words", keywords.joinToString(" "))
            .add("-meta.all", "1")
            .add("-ucd", ucd)
            .build()

    }
}
