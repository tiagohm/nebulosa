package nebulosa.api.guiding

import nebulosa.guiding.GuideState

data class GuiderInfo(
    val connected: Boolean = false,
    val state: GuideState = GuideState.STOPPED,
    val settling: Boolean = false,
    val pixelScale: Double = 1.0,
) {

    companion object {

        @JvmStatic val DISCONNECTED = GuiderInfo()
    }
}
