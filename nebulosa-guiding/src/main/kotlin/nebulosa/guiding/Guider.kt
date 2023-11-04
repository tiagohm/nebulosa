package nebulosa.guiding

import java.io.Closeable
import java.time.Duration

interface Guider : Closeable {

    val state: GuideState

    val pixelScale: Double

    val isSettling: Boolean

    var settleAmount: Double

    var settleTime: Duration

    var settleTimeout: Duration

    fun registerGuiderListener(listener: GuiderListener)

    fun unregisterGuiderListener(listener: GuiderListener)

    fun autoSelectGuideStar()

    fun startLooping(autoSelectGuideStar: Boolean = true)

    fun startGuiding(forceCalibration: Boolean = false, waitForSettle: Boolean = true)

    fun stopGuiding(force: Boolean = false)

    fun clearCalibration()

    val canDither: Boolean

    fun dither(amount: Double, raOnly: Boolean = false)

    fun waitForSettle()

    companion object {

        @JvmStatic val DEFAULT_SETTLE_AMOUNT = 1.5
        @JvmStatic val DEFAULT_SETTLE_TIME = Duration.ofSeconds(10)!!
        @JvmStatic val DEFAULT_SETTLE_TIMEOUT = Duration.ofSeconds(30)!!
    }
}
