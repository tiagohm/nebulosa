package nebulosa.api.cameras

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.camera.Camera
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class CameraDeserializer : StdDeserializer<Camera>(Camera::class.java) {

    @Autowired @Lazy private lateinit var connectionService: ConnectionService

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Camera? {
        val node = p.codec.readTree<JsonNode>(p)

        val name = if (node.has("camera")) node.get("camera").asText()
        else if (node.has("device")) node.get("device").asText()
        else return null

        return if (name.isNullOrBlank()) null
        else connectionService.camera(name)
    }
}
