package nebulosa.indi.device

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.indi.protocol.PropertyPermission
import nebulosa.indi.protocol.PropertyState
import nebulosa.json.HasJson

sealed interface PropertyVector<T, P : Property<T>> : Map<String, P>, HasJson {

    val device: Device

    val name: String

    val label: String

    val group: String

    val perm: PropertyPermission

    val state: PropertyState

    override fun writeToJson(gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("device", device.name)
        gen.writeStringField("name", name)
        gen.writeStringField("label", label)
        gen.writeStringField("group", group)
        gen.writeStringField("perm", perm.name)
        gen.writeStringField("state", state.name)
        gen.writeObjectField("items", values)

        when (this) {
            is NumberPropertyVector -> gen.writeStringField("type", "NUMBER")
            is TextPropertyVector -> gen.writeStringField("type", "TEXT")
            is SwitchPropertyVector -> {
                gen.writeStringField("type", "SWITCH")
                gen.writeStringField("rule", rule.name)
            }
        }

        gen.writeEndObject()
    }
}
