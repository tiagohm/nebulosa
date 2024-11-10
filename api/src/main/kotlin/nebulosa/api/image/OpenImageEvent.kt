package nebulosa.api.image

import nebulosa.api.message.MessageEvent
import java.nio.file.Path

data class OpenImageEvent(@JvmField val path: Path) : MessageEvent {

    override val eventName = "IMAGE.OPEN"
}
