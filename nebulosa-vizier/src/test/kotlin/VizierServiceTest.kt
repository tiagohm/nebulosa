import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.StringSpec
import nebulosa.vizier.VizierService

@Ignored
class VizierServiceTest : StringSpec() {

    init {
        "catalogs" {
            val vizier = VizierService()
        }
    }
}
