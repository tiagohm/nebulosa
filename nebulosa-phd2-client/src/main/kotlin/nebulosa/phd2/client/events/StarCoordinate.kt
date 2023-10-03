package nebulosa.phd2.client.events

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode

data class StarCoordinate(
    val x: Double, val y: Double,
) {

    class Deserializer : StdDeserializer<StarCoordinate>(Any::class.java) {

        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext,
        ): StarCoordinate {
            val node = p.codec.readTree<JsonNode>(p)
            val pos = node.get("pos") as? ArrayNode
            return if (pos == null) EMPTY
            else StarCoordinate(pos[0].asDouble(), pos[1].asDouble())
        }
    }

    companion object {

        @JvmStatic val EMPTY = StarCoordinate(Double.NaN, Double.NaN)
    }
}
