import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nebulosa.json.HasJson
import nebulosa.json.HasJsonModule
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HasJsonSerializerTest : StringSpec() {

    init {
        "supports json" {
            val objectMapper = ObjectMapper()
                .registerModule(HasJsonModule())

            val person = Person("Tiago", "Melo", 29, "12345678909", LocalDate.of(1994, 1, 28))
            val json = objectMapper.writeValueAsString(person)
            val jsonTree = objectMapper.readTree(json)

            jsonTree.get("name").asText() shouldBe "Tiago Melo"
            jsonTree.get("age").asInt() shouldBe 29
            jsonTree.get("taxId").shouldBeNull()
            jsonTree.get("birthDate").asText() shouldBe "1994/01/28"
        }
    }

    private data class Person(
        @JvmField val firstName: String,
        @JvmField val lastName: String,
        @JvmField val age: Int,
        @JvmField val taxId: String,
        @JvmField val birthDate: LocalDate,
    ) : HasJson {

        override fun writeToJson(gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeStartObject()
            gen.writeStringField("name", "$firstName $lastName")
            gen.writeNumberField("age", age)
            gen.writeStringField("birthDate", birthDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
            gen.writeEndObject()
        }
    }
}
