import com.fasterxml.jackson.module.kotlin.jsonMapper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.objectbox.kotlin.boxFor
import nebulosa.api.atlas.Location
import nebulosa.api.database.MyObjectBox
import nebulosa.api.preference.PreferenceEntity
import nebulosa.api.preference.PreferenceRepository
import nebulosa.api.preference.PreferenceService
import nebulosa.indi.device.camera.FrameType
import java.util.*

class PreferenceRepositoryTest : StringSpec() {

    init {
        val boxStore = MyObjectBox.builder()
            .inMemory(UUID.randomUUID().toString())
            .build()

        afterSpec {
            boxStore.close()
        }

        val box = boxStore.boxFor<PreferenceEntity>()
        val repository = PreferenceRepository(box)
        val service = PreferenceService(repository, jsonMapper { })

        "boolean" {
            service.contains("b").shouldBeFalse()
            service.putBoolean("b", true)
            service.contains("b").shouldBeTrue()
            service.getBoolean("b").shouldNotBeNull().shouldBeTrue()
            service.putBoolean("b", false)
            service.getBoolean("b").shouldNotBeNull().shouldBeFalse()
            service.delete("b")
            service.contains("b").shouldBeFalse()
            service.getBoolean("b").shouldBeNull()
        }
        "int" {
            service.contains("i").shouldBeFalse()
            service.putInt("i", 22)
            service.contains("i").shouldBeTrue()
            service.getInt("i").shouldNotBeNull() shouldBeExactly 22
            service.delete("i")
            service.contains("i").shouldBeFalse()
            service.getInt("i").shouldBeNull()
        }
        "long" {
            service.contains("l").shouldBeFalse()
            service.putLong("l", 22L)
            service.contains("l").shouldBeTrue()
            service.getLong("l").shouldNotBeNull() shouldBeExactly 22L
            service.delete("l")
            service.contains("l").shouldBeFalse()
            service.getLong("l").shouldBeNull()
        }
        "double" {
            service.contains("d").shouldBeFalse()
            service.putDouble("d", 22.0)
            service.contains("d").shouldBeTrue()
            service.getDouble("d").shouldNotBeNull() shouldBeExactly 22.0
            service.delete("d")
            service.contains("d").shouldBeFalse()
            service.getDouble("d").shouldBeNull()
        }
        "text" {
            service.contains("s").shouldBeFalse()
            service.putText("s", "Texto")
            service.contains("s").shouldBeTrue()
            service.getText("s").shouldNotBeNull() shouldBe "Texto"
            service.delete("s")
            service.contains("s").shouldBeFalse()
            service.getText("s").shouldBeNull()
        }
        "enum" {
            service.contains("e").shouldBeFalse()
            service.putEnum("e", FrameType.DARK)
            service.contains("e").shouldBeTrue()
            service.getEnum<FrameType>("e").shouldNotBeNull() shouldBe FrameType.DARK
            service.delete("e")
            service.contains("e").shouldBeFalse()
            service.getEnum<FrameType>("e").shouldBeNull()
        }
        "json" {
            service.contains("j").shouldBeFalse()
            service.putJSON("j", Location(longitude = 123.456))
            service.contains("j").shouldBeTrue()
            service.getJSON<Location>("j").shouldNotBeNull() shouldBe Location(longitude = 123.456)
            service.delete("j")
            service.contains("j").shouldBeFalse()
            service.getJSON<Location>("j").shouldBeNull()
        }
        "clear" {
            service.putLong("l", 22L)
            service.putDouble("d", 22.0)
            service.putText("s", "Texto")
            service.putEnum("e", FrameType.DARK)
            service.putJSON("j", Location(longitude = 123.456))
            service.size shouldBeExactly 5
            service.clear()
            service.isEmpty().shouldBeTrue()
        }
    }
}
