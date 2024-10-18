import com.fasterxml.jackson.module.kotlin.jsonMapper
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.api.atlas.Location
import nebulosa.api.database.MainDatabaseMigrator
import nebulosa.api.preference.PreferenceRepository
import nebulosa.api.preference.PreferenceService
import nebulosa.indi.device.camera.FrameType
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class PreferenceServiceTest {

    @Test
    fun boolean() {
        SERVICE.contains("b").shouldBeFalse()
        SERVICE.putBoolean("b", true)
        SERVICE.contains("b").shouldBeTrue()
        SERVICE.getBoolean("b").shouldNotBeNull().shouldBeTrue()
        SERVICE.putBoolean("b", false)
        SERVICE.getBoolean("b").shouldNotBeNull().shouldBeFalse()
        SERVICE.delete("b").shouldBeTrue()
        SERVICE.contains("b").shouldBeFalse()
        SERVICE.getBoolean("b").shouldBeNull()
    }

    @Test
    fun int() {
        SERVICE.contains("i").shouldBeFalse()
        SERVICE.putInt("i", 22)
        SERVICE.contains("i").shouldBeTrue()
        SERVICE.getInt("i").shouldNotBeNull() shouldBeExactly 22
        SERVICE.delete("i").shouldBeTrue()
        SERVICE.contains("i").shouldBeFalse()
        SERVICE.getInt("i").shouldBeNull()
    }

    @Test
    fun long() {
        SERVICE.contains("l").shouldBeFalse()
        SERVICE.putLong("l", 22L)
        SERVICE.contains("l").shouldBeTrue()
        SERVICE.getLong("l").shouldNotBeNull() shouldBeExactly 22L
        SERVICE.delete("l").shouldBeTrue()
        SERVICE.contains("l").shouldBeFalse()
        SERVICE.getLong("l").shouldBeNull()
    }

    @Test
    fun double() {
        SERVICE.contains("d").shouldBeFalse()
        SERVICE.putDouble("d", 22.0)
        SERVICE.contains("d").shouldBeTrue()
        SERVICE.getDouble("d").shouldNotBeNull() shouldBeExactly 22.0
        SERVICE.delete("d").shouldBeTrue()
        SERVICE.contains("d").shouldBeFalse()
        SERVICE.getDouble("d").shouldBeNull()
    }

    @Test
    fun text() {
        SERVICE.contains("s").shouldBeFalse()
        SERVICE.putText("s", "Texto")
        SERVICE.contains("s").shouldBeTrue()
        SERVICE.getText("s").shouldNotBeNull() shouldBe "Texto"
        SERVICE.delete("s").shouldBeTrue()
        SERVICE.contains("s").shouldBeFalse()
        SERVICE.getText("s").shouldBeNull()
    }

    @Test
    fun enum() {
        SERVICE.contains("e").shouldBeFalse()
        SERVICE.putEnum("e", FrameType.DARK)
        SERVICE.contains("e").shouldBeTrue()
        SERVICE.getEnum<FrameType>("e").shouldNotBeNull() shouldBe FrameType.DARK
        SERVICE.delete("e").shouldBeTrue()
        SERVICE.contains("e").shouldBeFalse()
        SERVICE.getEnum<FrameType>("e").shouldBeNull()
    }

    @Test
    fun json() {
        SERVICE.contains("j").shouldBeFalse()
        SERVICE.putJSON("j", Location(longitude = 123.456))
        SERVICE.contains("j").shouldBeTrue()
        SERVICE.getJSON<Location>("j").shouldNotBeNull() shouldBe Location(longitude = 123.456)
        SERVICE.delete("j").shouldBeTrue()
        SERVICE.contains("j").shouldBeFalse()
        SERVICE.getJSON<Location>("j").shouldBeNull()
    }

    @Test
    fun clear() {
        SERVICE.putLong("l", 22L)
        SERVICE.putDouble("d", 22.0)
        SERVICE.putText("s", "Texto")
        SERVICE.putEnum("e", FrameType.DARK)
        SERVICE.putJSON("j", Location(longitude = 123.456))
        SERVICE.size shouldBeExactly 5
        SERVICE.clear()
        SERVICE.size shouldBeExactly 0
    }

    companion object {

        private const val DATASOURCE = "jdbc:h2:mem:preference;DB_CLOSE_DELAY=-1"

        private val CONNECTION = Database.connect(DATASOURCE, user = "root", password = "")

        @AfterAll
        @JvmStatic
        fun closeConnection() {
            TransactionManager.closeAndUnregister(CONNECTION)
        }

        init {
            MainDatabaseMigrator(DATASOURCE).run()
        }

        @JvmStatic private val REPOSITORY = PreferenceRepository(CONNECTION)
        @JvmStatic private val SERVICE = PreferenceService(REPOSITORY, jsonMapper { })
    }
}
