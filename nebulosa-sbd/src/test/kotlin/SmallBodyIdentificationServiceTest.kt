import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Distance.Companion.km
import nebulosa.sbd.SmallBodyDatabaseService
import java.time.LocalDateTime

class SmallBodyIdentificationServiceTest : StringSpec() {

    init {
        val service = SmallBodyDatabaseService()

        "search around Ceres" {
            val data = service.identify(
                LocalDateTime.of(2023, 8, 21, 0, 0, 0, 0),
                // Observatorio do Pico dos Dias, Itajuba (observatory) [code: 874]
                (-22.5354318).deg, (-45.5827).deg, 1.81754.km,
                Angle.from("13 21 16.50", true), Angle.from("-01 57 06.5"), 1.0.deg,
            ).execute().body()

            data.shouldNotBeNull().data.any { "Ceres" in it[0] }.shouldBeTrue()
        }
    }
}
