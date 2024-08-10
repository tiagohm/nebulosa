package nebulosa.api.guiding

import nebulosa.guiding.GuideState

data class GuiderInfo(
    @JvmField val connected: Boolean = false,
    @JvmField val state: GuideState = GuideState.STOPPED,
    @JvmField val settling: Boolean = false,
    @JvmField val pixelScale: Double = 1.0,
) {

    companion object {

        @JvmStatic val DISCONNECTED = GuiderInfo()
    }
}
