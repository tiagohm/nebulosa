import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.api.calibration.CalibrationFrameEntity
import nebulosa.api.calibration.CalibrationFrameRepository
import nebulosa.api.database.migration.MainDatabaseMigrator
import nebulosa.indi.device.camera.FrameType
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class CalibrationFrameRepositoryTest {

    @Test
    fun findAll() {
        REPOSITORY.findAll().shouldHaveSize(17)
    }

    @Test
    fun finalAllByGroup() {
        REPOSITORY.findAll(NAME).shouldHaveSize(17)
    }

    @Test
    fun getById() {
        with(REPOSITORY[1L].shouldNotBeNull()) {
            type shouldBe FrameType.DARK
            exposureTime shouldBeExactly 1L
        }
    }

    @Test
    fun findDarks() {
        REPOSITORY.darkFrames(NAME, 1280, 1024, 1, 1L, 0.0).shouldHaveSize(1)
        REPOSITORY.darkFrames(NAME, 1280, 1024, 1, 60L, 0.0).shouldHaveSize(4)
        REPOSITORY.darkFrames(NAME, 1280, 1024, 1, 60L, 100.0).shouldHaveSize(2)
        REPOSITORY.darkFrames(NAME, 1280, 1024, 1, 60L, 50.0).shouldBeEmpty()
        REPOSITORY.darkFrames(NAME, 1280, 1024, 2, 60L, 100.0).shouldBeEmpty()
        REPOSITORY.darkFrames(NAME, 4092, 2800, 1, 60L, 100.0).shouldBeEmpty()
        REPOSITORY.darkFrames("ZW", 1280, 1024, 1, 1L, 0.0).shouldBeEmpty()
    }

    @Test
    fun findBias() {
        REPOSITORY.biasFrames(NAME, 1280, 1024, 1, 0.0).shouldHaveSize(2)
        REPOSITORY.biasFrames(NAME, 1280, 1024, 1, 100.0).shouldHaveSize(1)
        REPOSITORY.biasFrames(NAME, 1280, 1024, 1, 50.0).shouldBeEmpty()
        REPOSITORY.biasFrames(NAME, 1280, 1024, 2, 0.0).shouldBeEmpty()
        REPOSITORY.biasFrames(NAME, 4092, 2800, 1, 0.0).shouldBeEmpty()
        REPOSITORY.biasFrames("ZW", 1280, 1024, 2, 0.0).shouldBeEmpty()
    }

    @Test
    fun findFlats() {
        REPOSITORY.flatFrames(NAME, null, 1280, 1024, 1).shouldHaveSize(1)
        REPOSITORY.flatFrames(NAME, "RED", 1280, 1024, 1).shouldHaveSize(1)
        REPOSITORY.flatFrames(NAME, "green", 1280, 1024, 1).shouldHaveSize(1)
        REPOSITORY.flatFrames(NAME, "BLUE", 1280, 1024, 1).shouldHaveSize(1)
        REPOSITORY.flatFrames(NAME, "RED", 1280, 1024, 2).shouldBeEmpty()
        REPOSITORY.flatFrames(NAME, "RED", 4092, 2800, 2).shouldBeEmpty()
        REPOSITORY.flatFrames(NAME, "HA", 1280, 1024, 2).shouldBeEmpty()
        REPOSITORY.flatFrames("ZW", "RED", 1280, 1024, 2).shouldBeEmpty()
    }

    companion object {

        private const val NAME = "CCD Simulator"
        private const val DATASOURCE = "jdbc:h2:mem:cf;DB_CLOSE_DELAY=-1"

        private val CONNECTION = Database.connect(DATASOURCE, user = "root", password = "")

        @AfterAll
        @JvmStatic
        fun closeConnection() {
            TransactionManager.closeAndUnregister(CONNECTION)
        }

        init {
            MainDatabaseMigrator(DATASOURCE).run()
        }

        private val REPOSITORY = CalibrationFrameRepository(CONNECTION).apply {
            save(FrameType.DARK, 1L)
            save(FrameType.DARK, 2L)
            save(FrameType.DARK, 5L)
            save(FrameType.DARK, 10L)
            save(FrameType.DARK, 30L)
            save(FrameType.DARK, 60L)
            save(FrameType.DARK, 60L, gain = 100.0)
            save(FrameType.DARK, 10L, temperature = -10.0)
            save(FrameType.DARK, 30L, temperature = -10.0)
            save(FrameType.DARK, 60L, temperature = -10.0)
            save(FrameType.DARK, 60L, temperature = -10.0, gain = 100.0)
            save(FrameType.BIAS, 0L)
            save(FrameType.BIAS, 0L, gain = 100.0)
            save(FrameType.FLAT, 0L, filter = "RED")
            save(FrameType.FLAT, 0L, filter = "GREEN")
            save(FrameType.FLAT, 0L, filter = "BLUE")
            save(FrameType.FLAT, 0L, filter = null)
        }

        @Suppress("NOTHING_TO_INLINE")
        internal inline fun CalibrationFrameRepository.save(
            type: FrameType, exposureTime: Long,
            temperature: Double = 25.0, width: Int = 1280, height: Int = 1024,
            bin: Int = 1, gain: Double = 0.0, filter: String? = null,
        ) = add(CalibrationFrameEntity(0L, type, NAME, filter, exposureTime, temperature, width, height, bin, bin, gain))
    }
}
