import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.objectbox.kotlin.boxFor
import nebulosa.api.atlas.SatelliteEntity
import nebulosa.api.atlas.SatelliteGroupType
import nebulosa.api.atlas.SatelliteRepository
import nebulosa.api.database.MyObjectBox
import java.util.*

class SatelliteEntityRepositoryTest : StringSpec() {

    init {
        val boxStore = MyObjectBox.builder()
            .inMemory(UUID.randomUUID().toString())
            .build()

        afterSpec {
            boxStore.close()
        }

        val box = boxStore.boxFor<SatelliteEntity>()
        val repository = SatelliteRepository(box)

        repository.save("ISS", SatelliteGroupType.ACTIVE, SatelliteGroupType.EDUCATION)
        repository.save("StarLink", SatelliteGroupType.ACTIVE, SatelliteGroupType.STARLINK)

        "find all" {
            repository.search().shouldHaveSize(2)
        }
        "find by name" {
            repository.search("iss").shouldHaveSize(1)
        }
        "find by groups" {
            repository.search(groups = listOf(SatelliteGroupType.ACTIVE)).shouldHaveSize(2)
            repository.search(groups = listOf(SatelliteGroupType.STARLINK)).shouldHaveSize(1)
            repository.search(groups = listOf(SatelliteGroupType.AMATEUR)).shouldBeEmpty()
        }
        "find by name and groups" {
            repository.search(text = "iss", groups = listOf(SatelliteGroupType.ACTIVE)).shouldHaveSize(1)
            repository.search(text = "iss", groups = listOf(SatelliteGroupType.STARLINK)).shouldBeEmpty()
            repository.search(text = "starlink", groups = listOf(SatelliteGroupType.EDUCATION)).shouldBeEmpty()
        }
    }

    companion object {

        @JvmStatic
        internal fun SatelliteRepository.save(name: String, vararg groups: SatelliteGroupType) {
            save(SatelliteEntity(0L, name, "", groups.map { it.name }.toMutableList()))
        }
    }
}
