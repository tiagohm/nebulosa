package nebulosa.guiding

import kotlin.time.Duration

interface Guider {

    val state: GuideState

    var settlePixels: Double

    var settleTime: Duration

    var settleTimeout: Duration

    fun registerGuideListener(listener: GuideListener)

    fun unregisterGuideListener(listener: GuideListener)

    fun autoSelectGuideStar()

    fun startGuiding(forceCalibration: Boolean = false, waitForSettle: Boolean = true)

    fun stopGuiding()

    fun clearCalibration()

    fun dither(pixels: Double, raOnly: Boolean = false)
}
