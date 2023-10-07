package nebulosa.guiding

import java.time.Duration

interface Guider {

    val state: GuideState

    val isSettling: Boolean

    var settlePixels: Double

    var settleTime: Duration

    var settleTimeout: Duration

    fun registerGuiderListener(listener: GuiderListener)

    fun unregisterGuiderListener(listener: GuiderListener)

    fun autoSelectGuideStar()

    fun startLooping(autoSelectGuideStar: Boolean = true)

    fun startGuiding(forceCalibration: Boolean = false, waitForSettle: Boolean = true)

    fun stopGuiding()

    fun clearCalibration()

    fun dither(pixels: Double, raOnly: Boolean = false)
}
