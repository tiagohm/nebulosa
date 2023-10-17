package nebulosa.guiding

import java.io.Closeable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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

    fun dither(amount: Double, raOnly: Boolean = false)

    fun waitForSettle()

    companion object {

        @JvmStatic val DEFAULT_SETTLE_AMOUNT = 1.5
        @JvmStatic val DEFAULT_SETTLE_TIME = 10.seconds
        @JvmStatic val DEFAULT_SETTLE_TIMEOUT = 30.seconds
    }
}
