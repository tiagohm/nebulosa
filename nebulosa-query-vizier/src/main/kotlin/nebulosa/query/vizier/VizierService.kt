package nebulosa.query.vizier

import nebulosa.query.QueryService

/**
 * @see <a href="http://cdsarc.u-strasbg.fr/doc/asu-summary.htx">Documentation</a>
 */
class VizierService(url: String = "http://vizier.cfa.harvard.edu") : QueryService(url), Vizier {

    private val service by lazy { retrofit.create(Vizier::class.java) }

    override fun query(
        words: String?, source: String?,
        kw: String?, ucd: String?, metaAll: String,
    ) {
        return service.query(words, source, kw, ucd, metaAll)
    }
}
