package nebulosa.api.wizard.flat

import nebulosa.api.messages.MessageEvent
import java.nio.file.Path
import java.time.Duration

data class FlatFrameCaptured(
    val savedPath: Path,
    val duration: Duration,
) : MessageEvent {

    override val eventName = "FLAT_FRAME_CAPTURED"
}
