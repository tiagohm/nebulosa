import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.objectbox.kotlin.boxFor
import nebulosa.api.calibration.CalibrationFrameEntity
import nebulosa.api.calibration.CalibrationFrameRepository
import nebulosa.api.database.MyObjectBox
import nebulosa.indi.device.camera.FrameType
import java.util.*

class CalibrationFrameRepositoryTest : StringSpec() {

    init {
        val boxStore = MyObjectBox.builder()
            .inMemory(UUID.randomUUID().toString())
            .build()

        afterSpec {
            boxStore.close()
        }

        val box = boxStore.boxFor<CalibrationFrameEntity>()
        val repository = CalibrationFrameRepository(box)

        repository.save(FrameType.DARK, 1L)
        repository.save(FrameType.DARK, 2L)
        repository.save(FrameType.DARK, 5L)
        repository.save(FrameType.DARK, 10L)
        repository.save(FrameType.DARK, 30L)
        repository.save(FrameType.DARK, 60L)
        repository.save(FrameType.DARK, 60L, gain = 100.0)
        repository.save(FrameType.DARK, 10L, temperature = -10.0)
        repository.save(FrameType.DARK, 30L, temperature = -10.0)
        repository.save(FrameType.DARK, 60L, temperature = -10.0)
        repository.save(FrameType.DARK, 60L, temperature = -10.0, gain = 100.0)
        repository.save(FrameType.BIAS, 0L)
        repository.save(FrameType.BIAS, 0L, gain = 100.0)
        repository.save(FrameType.FLAT, 0L, filter = "RED")
        repository.save(FrameType.FLAT, 0L, filter = "GREEN")
        repository.save(FrameType.FLAT, 0L, filter = "BLUE")
        repository.save(FrameType.FLAT, 0L, filter = null)

        "find all" {
            repository.findAll().shouldHaveSize(17)
        }
        "find darks" {
            repository.darkFrames(NAME, 1280, 1024, 1, 1L, 0.0).shouldHaveSize(1)
            repository.darkFrames(NAME, 1280, 1024, 1, 60L, 0.0).shouldHaveSize(4)
            repository.darkFrames(NAME, 1280, 1024, 1, 60L, 100.0).shouldHaveSize(2)
            repository.darkFrames(NAME, 1280, 1024, 1, 60L, 50.0).shouldBeEmpty()
            repository.darkFrames(NAME, 1280, 1024, 2, 60L, 100.0).shouldBeEmpty()
            repository.darkFrames(NAME, 4092, 2800, 1, 60L, 100.0).shouldBeEmpty()
            repository.darkFrames("ZW", 1280, 1024, 1, 1L, 0.0).shouldBeEmpty()
        }
        "find bias" {
            repository.biasFrames(NAME, 1280, 1024, 1, 0.0).shouldHaveSize(2)
            repository.biasFrames(NAME, 1280, 1024, 1, 100.0).shouldHaveSize(1)
            repository.biasFrames(NAME, 1280, 1024, 1, 50.0).shouldBeEmpty()
            repository.biasFrames(NAME, 1280, 1024, 2, 0.0).shouldBeEmpty()
            repository.biasFrames(NAME, 4092, 2800, 1, 0.0).shouldBeEmpty()
            repository.biasFrames("ZW", 1280, 1024, 2, 0.0).shouldBeEmpty()
        }
        "find flats" {
            repository.flatFrames(NAME, null, 1280, 1024, 1).shouldHaveSize(1)
            repository.flatFrames(NAME, "RED", 1280, 1024, 1).shouldHaveSize(1)
            repository.flatFrames(NAME, "green", 1280, 1024, 1).shouldHaveSize(1)
            repository.flatFrames(NAME, "BLUE", 1280, 1024, 1).shouldHaveSize(1)
            repository.flatFrames(NAME, "RED", 1280, 1024, 2).shouldBeEmpty()
            repository.flatFrames(NAME, "RED", 4092, 2800, 2).shouldBeEmpty()
            repository.flatFrames(NAME, "HA", 1280, 1024, 2).shouldBeEmpty()
            repository.flatFrames("ZW", "RED", 1280, 1024, 2).shouldBeEmpty()
        }
    }

    companion object {

        private const val NAME = "CCD Simulator"

        @JvmStatic
        internal fun CalibrationFrameRepository.save(
            type: FrameType, exposureTime: Long,
            temperature: Double = 25.0, width: Int = 1280, height: Int = 1024,
            bin: Int = 1, gain: Double = 0.0,
            filter: String? = null,
        ) {
            save(CalibrationFrameEntity(0L, type, NAME, filter, exposureTime, temperature, width, height, bin, bin, gain))
        }
    }
}
