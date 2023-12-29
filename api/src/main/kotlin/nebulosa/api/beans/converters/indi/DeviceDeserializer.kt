package nebulosa.api.beans.converters.indi

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode

sealed class DeviceDeserializer<T>(type: Class<out T>) : StdDeserializer<T>(type) {

    protected abstract fun deviceFor(name: String): T?

    final override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T? {
        val node = p.codec.readTree<JsonNode>(p)

        return if (node is TextNode) {
            deviceFor(node.asText())
        } else if (node.has("name") && node.get("name") is TextNode) {
            deviceFor(node.get("name").asText())
        } else {
            null
        }
    }
}
