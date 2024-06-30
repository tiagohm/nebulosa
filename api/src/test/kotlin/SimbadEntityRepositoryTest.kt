import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.objectbox.kotlin.boxFor
import nebulosa.api.atlas.SimbadEntity
import nebulosa.api.atlas.SimbadEntityRepository
import nebulosa.api.database.MyObjectBox
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import java.util.*

class SimbadEntityRepositoryTest : StringSpec() {

    init {
        val boxStore = MyObjectBox.builder()
            .inMemory(UUID.randomUUID().toString())
            .build()

        afterSpec {
            boxStore.close()
        }

        val box = boxStore.boxFor<SimbadEntity>()
        val repository = SimbadEntityRepository(box)

        repository.save("Sirius", SkyObjectType.STAR, Constellation.CMA, -1.45, "06 45 06".hours, "-16 43 33".deg)
        repository.save("Dolphin Nebula", SkyObjectType.NEBULA, Constellation.CMA, 6.91, "06 54 11".hours, "-23 55 47".deg)
        repository.save("75 Tucanae", SkyObjectType.GLOBULAR_CLUSTER, Constellation.TUC, 6.58, "01 03 12".hours, "-70 50 39".deg)
        repository.save("Car Nebula", SkyObjectType.NEBULA, Constellation.CAR, 5.0, "10 45 15".hours, "-59 43 35".deg)

        "find all" {
            repository.search().shouldHaveSize(4).first().magnitude shouldBeExactly -1.45
        }
        "find by name" {
            repository.search(name = "dolphin").shouldHaveSize(1).first().name shouldBe "Dolphin Nebula"
            repository.search(name = "andromeda").shouldBeEmpty()
            repository.search(name = "nebula").shouldHaveSize(2).first().magnitude shouldBeExactly 5.0
        }
        "find by constellation" {
            repository.search(constellation = Constellation.CMA).shouldHaveSize(2).first().magnitude shouldBeExactly -1.45
            repository.search(constellation = Constellation.AND).shouldBeEmpty()
        }
        "find by region" {
            repository.search(rightAscension = "06 45 59".hours, declination = "-20 45 29".deg, radius = 4.5.deg).shouldHaveSize(2)
                .first().magnitude shouldBeExactly -1.45
            repository.search(rightAscension = "06 45 59".hours, declination = "-20 45 29".deg, radius = 4.0.deg).shouldHaveSize(1)
                .first().name shouldBe "Dolphin Nebula"
            repository.search(rightAscension = "00 42 43".hours, declination = "41 15 53".deg, radius = 10.deg).shouldBeEmpty()
        }
        "find by magnitude" {
            repository.search(magnitudeMin = 5.0).shouldHaveSize(3)
            repository.search(magnitudeMax = 4.9).shouldHaveSize(1).first().name shouldBe "Sirius"
            repository.search(magnitudeMin = 6.6, magnitudeMax = 6.99).shouldHaveSize(1).first().name shouldBe "Dolphin Nebula"
            repository.search(magnitudeMax = -2.0).shouldBeEmpty()
            repository.search(magnitudeMin = 7.0).shouldBeEmpty()
            repository.search(magnitudeMin = 5.1, magnitudeMax = 6.0).shouldBeEmpty()
        }
        "find by type" {
            repository.search(type = SkyObjectType.NEBULA).shouldHaveSize(2).first().magnitude shouldBeExactly 5.0
            repository.search(type = SkyObjectType.GALAXY).shouldBeEmpty()
        }
    }

    companion object {

        @JvmStatic
        internal fun SimbadEntityRepository.save(
            name: String, type: SkyObjectType, constellation: Constellation,
            magnitude: Double, rightAscension: Angle, declination: Angle,
        ) {
            save(SimbadEntity(0L, name, type, rightAscension, declination, magnitude, constellation = constellation))
        }
    }
}
