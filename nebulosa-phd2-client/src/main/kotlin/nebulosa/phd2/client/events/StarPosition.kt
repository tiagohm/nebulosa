package nebulosa.phd2.client.events

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import nebulosa.guiding.GuidePoint

data class StarPosition(override val x: Int = 0, override val y: Int = 0) : GuidePoint {

    class Deserializer : StdDeserializer<StarPosition>(Any::class.java) {

        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext,
        ): StarPosition {
            val node = p.codec.readTree<JsonNode>(p)

            return if (node is ArrayNode) {
                StarPosition(node[0].asInt(), node[1].asInt())
            } else if (node.has("pos")) {
                val pos = node.get("pos") as ArrayNode
                StarPosition(pos[0].asInt(), pos[1].asInt())
            } else {
                ZERO
            }
        }
    }

    companion object {

        @JvmStatic val ZERO = StarPosition()
    }
}
