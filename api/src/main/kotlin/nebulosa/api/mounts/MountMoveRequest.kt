package nebulosa.api.mounts

import nebulosa.guiding.GuideDirection
import java.time.Duration

data class MountMoveRequest(
    @JvmField val direction: GuideDirection,
    @JvmField val duration: Duration,
    @JvmField val speed: String? = null,
)
