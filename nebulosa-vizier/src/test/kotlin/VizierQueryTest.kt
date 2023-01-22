import io.kotest.core.spec.style.StringSpec
import nebulosa.query.vizier.VizierQuery

class VizierQueryTest : StringSpec() {

    init {
        "catalogs" {
            val vizier = VizierQuery()
            vizier.catalogs("Kang W51")
        }
    }
}
