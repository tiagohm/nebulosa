import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.math.km
import nebulosa.sbd.SmallBodyDatabaseService
import nebulosa.test.HTTP_CLIENT
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SmallBodyIdentificationServiceTest {

    @Test
    fun searchAroundCeres() {
        val data = SERVICE.identify(
            LocalDateTime.of(2023, 8, 21, 0, 0, 0, 0),
            // Observatorio do Pico dos Dias, Itajuba (observatory) [code: 874]
            (-22.5354318).deg, (-45.5827).deg, 1.81754.km,
            "13 21 16.50".hours, "-01 57 06.5".deg, 1.0.deg,
        ).execute().body()

        data.shouldNotBeNull()
        data.count shouldBeGreaterThanOrEqual 1
        data.data.any { "Ceres" in it[0] }.shouldBeTrue()
    }

    @Test
    fun noMatchingRecords() {
        val data = SERVICE.identify(
            LocalDateTime.of(2023, 1, 15, 1, 38, 15, 0),
            // Observatorio do Pico dos Dias, Itajuba (observatory) [code: 874]
            (-22.5354318).deg, (-45.5827).deg, 1.81754.km,
            "10 44 02".hours, "-59 36 04".deg, 1.0.deg,
        ).execute().body()

        data.shouldNotBeNull()
        data.count shouldBeExactly 0
        data.data.shouldBeEmpty()
    }

    companion object {

        private val SERVICE = SmallBodyDatabaseService(httpClient = HTTP_CLIENT)
    }
}
