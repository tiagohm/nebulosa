package nebulosa.api.beans.converters

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode

abstract class DeviceDeserializer<T>(type: Class<out T>) : StdDeserializer<T>(type) {

    protected abstract val names: Iterable<String>

    protected abstract fun device(name: String): T?

    final override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T? {
        val node = p.codec.readTree<JsonNode>(p)

        if (node is TextNode) {
            return device(node.asText())
        }

        for (name in names) {
            if (node.has(name)) {
                val deviceNode = node.get(name)

                if (deviceNode is TextNode) {
                    return device(deviceNode.asText()) ?: continue
                } else if (deviceNode.has("name")) {
                    return device(deviceNode.get("name").asText()) ?: continue
                }
            }
        }

        return null
    }
}
