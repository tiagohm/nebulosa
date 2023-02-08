import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.StringSpec
import nebulosa.query.vizier.VizierQuery

@Ignored
class VizierQueryTest : StringSpec() {

    init {
        "catalogs" {
            val vizier = VizierQuery()
            vizier.catalogs("Kang W51")
        }
    }
}
