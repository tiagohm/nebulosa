package nebulosa.guiding.phd2

import nebulosa.guiding.GuidePoint
import nebulosa.guiding.GuideStar
import nebulosa.guiding.Guider
import nebulosa.guiding.GuiderListener
import nebulosa.io.Base64OutputStream
import nebulosa.log.loggerFor
import nebulosa.math.arcsec
import nebulosa.math.toArcsec
import nebulosa.phd2.client.PHD2Client
import nebulosa.phd2.client.PHD2EventListener
import nebulosa.phd2.client.commands.*
import nebulosa.phd2.client.events.*
import java.io.Closeable
import java.time.Duration
import javax.imageio.ImageIO
import kotlin.math.min
import kotlin.time.toKotlinDuration

class PHD2Guider(private val client: PHD2Client) : Guider, PHD2EventListener, Closeable {

    private val dither = DoubleArray(2)
    @Volatile private var settling = false
    @Volatile private var pixelScale = 0.0
    @Volatile private var shiftRate = SiderealShiftTrackingRate.DISABLED
    @Volatile private var shiftEnabled = false
    @Volatile private var shiftRateAxis = ShiftAxesType.RADEC
    @Volatile private var lockPosition = GuidePoint.ZERO
    @Volatile private var starPosition = GuidePoint.ZERO
    private val listeners = hashSetOf<GuiderListener>()

    override var state = State.STOPPED
        private set(value) {
            if (value != field) {
                field = value
                listeners.forEach { it.onStateChange(value) }
            }
        }

    override val isSettling
        get() = settling

    init {
        client.registerListener(this)
    }

    override var settlePixels = 1.5
    override var settleTime = Duration.ofSeconds(10)!!
    override var settleTimeout = Duration.ofSeconds(30)!!

    override fun registerGuiderListener(listener: GuiderListener) {
        listeners.add(listener)
    }

    override fun unregisterGuiderListener(listener: GuiderListener) {
        listeners.remove(listener)
    }

    private inline fun fireMessage(lazyMessage: () -> String) {
        if (listeners.isNotEmpty()) {
            val message = lazyMessage()
            listeners.forEach { it.onMessage(message) }
        }
    }

    override fun startLooping(autoSelectGuideStar: Boolean) {
        val state = client.sendCommandSync(GetAppState)

        if (state == State.STOPPED) {
            if (autoSelectGuideStar) {
                autoSelectGuideStar()
            } else {
                client.sendCommandSync(Loop)
            }
        } else if (state == State.LOOPING) {
            if (autoSelectGuideStar) {
                autoSelectGuideStar()
            }
        }
    }

    override fun startGuiding(forceCalibration: Boolean, waitForSettle: Boolean) {
        val state = client.sendCommandSync(GetAppState)

        if (state == State.GUIDING) {
            LOG.info("app is already guiding. skipping start guiding")
            return
        }

        if (state == State.LOST_LOCK) {
            LOG.info("app lost guide star and needs to stop before starting guiding again")
            stopGuiding()
        }

        if (state == State.CALIBRATING) {
            LOG.info("app is already calibrating. waiting for calibration to finish")
            waitForCalibrationFinished()
        }

        val isCalibrated = forceCalibration || client.sendCommandSync(GetCalibrated)

        startGuide(forceCalibration)
        val starSelected = waitForStarSelected()

        if (starSelected != null) {
            if (!isCalibrated) {
                Thread.sleep(5000)
                waitForCalibrationFinished()
            }

            waitForGuidingStarted()

            if (waitForSettle) {
                waitForSettling()
            }

            return
        } else {
            fireMessage { "failed to select star" }
        }

        Thread.sleep(1000)

        stopGuiding()
    }

    override fun stopGuiding() {
        val state = client.sendCommandSync(GetAppState)

        if (state != State.GUIDING && state != State.CALIBRATING && state != State.LOST_LOCK) {
            fireMessage { "stop guiding skipped, as the app is already in state $state" }
            return
        }

        client.sendCommandSync(StopCapture)
    }

    override fun autoSelectGuideStar() {
        val state = client.sendCommandSync(GetAppState)

        if (state != State.LOOPING) {
            client.sendCommandSync(Loop)
            waitForState(State.LOOPING)
        }

        // Wait for at least one exposure to finish.
        val exposureTime = client.sendCommandSync(GetExposure)
        Thread.sleep(exposureTime + 1000)

        client.sendCommandSync(FindStar())
    }

    override fun clearCalibration() {
        client.sendCommandSync(ClearCalibration(WhichMount.BOTH))
        Thread.sleep(100)
    }

    override fun dither(pixels: Double, raOnly: Boolean) {
        val state = client.sendCommandSync(GetAppState)

        if (state == State.GUIDING) {
            waitForSettling()

            client.sendCommandSync(Dither(pixels, raOnly, settlePixels, settleTime.toKotlinDuration(), settleTimeout.toKotlinDuration()))

            settling = true
            waitForSettling()
        }
    }

    private fun waitForCalibrationFinished() {
        while (true) {
            val state = client.sendCommandSync(GetAppState)
            if (state != State.CALIBRATING) break
            Thread.sleep(1000)
        }
    }

    private fun waitForGuidingStarted() {
        waitForState(State.GUIDING)
        settling = true
    }

    private fun waitForState(state: State) {
        while (true) {
            if (client.sendCommandSync(GetAppState) == state) break
            Thread.sleep(1000)
        }
    }

    private fun waitForStarSelected(): IntArray? {
        repeat(5) {
            try {
                return client.sendCommandSync(GetLockPosition, 5)
            } catch (ignored: Throwable) {
                Thread.sleep(5000)
            }
        }

        return null
    }

    private fun startGuide(forceCalibration: Boolean): Boolean {
        return try {
            waitForSettling()
            val command = Guide(settlePixels, settleTime.toKotlinDuration(), settleTimeout.toKotlinDuration(), forceCalibration)
            client.sendCommandSync(command)
            refreshShiftLockParams()
            true
        } catch (e: Throwable) {
            false
        }
    }

    private fun refreshShiftLockParams() {
        val shiftParams = client.sendCommandSync(GetLockShiftParams)

        if (shiftParams.enabled) {
            shiftRate = if (shiftParams.units == RateUnit.PIXELS_HOUR) {
                val (raShiftRate, decShiftRate) = shiftParams.rate
                SiderealShiftTrackingRate((raShiftRate * pixelScale).arcsec, (decShiftRate * pixelScale).arcsec)
            } else {
                val (raShiftRate, decShiftRate) = shiftParams.rate
                SiderealShiftTrackingRate(raShiftRate.arcsec, decShiftRate.arcsec)
            }

            shiftRateAxis = shiftParams.axes
            shiftEnabled = true

            LOG.info("shift lock params refreshed. rate={}, axes={}", shiftRate, shiftRateAxis)
        } else {
            shiftEnabled = false
        }
    }

    private fun waitForSettling() {
        val startTime = System.currentTimeMillis()
        val settleTimeout = min(30000L, settleTimeout.toMillis())

        while (settling) {
            Thread.sleep(500)

            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - startTime

            if (elapsedTime >= settleTimeout) {
                LOG.warn("PHD2 did not send SettleDone message in expected time")
                settling = false
                break
            }
        }
    }

    private fun restartForLostShiftLock() {
        stopGuiding()
        // Don't wait for settling when restarting due
        // to lost lock shift, which should minimize downtime.
        startGuiding(waitForSettle = false)
        updateShiftRate(shiftRate)
    }

    private fun updateShiftRate(shiftTrackingRate: SiderealShiftTrackingRate) {
        if (!shiftTrackingRate.enabled) {
            return stopShifting()
        }

        shiftRate = shiftTrackingRate
        val raArcsecPerHour = shiftTrackingRate.raPerHour.toArcsec
        val decArcsecPerHour = shiftTrackingRate.decPerHour.toArcsec

        LOG.info("setting shift rate. ra={}, dec={}", raArcsecPerHour, decArcsecPerHour)

        val command = SetLockShiftParams(raArcsecPerHour, decArcsecPerHour, ShiftAxesType.RADEC, RateUnit.ARCSEC_HOUR)
        client.sendCommandSync(command)
        client.sendCommandSync(SetLockShiftEnabled(true))

        refreshShiftLockParams()
    }

    private fun stopShifting() {
        if (!shiftEnabled) return
        client.sendCommandSync(SetLockShiftEnabled(false))
        refreshShiftLockParams()
    }

    override fun onEventReceived(event: PHD2Event) {
        LOG.info("event received: {}", event)

        when (event) {
            is AlertEvent -> Unit
            is AppStateEvent -> state = event.state
            is CalibratingEvent -> {
                fireMessage { "${event.state}. step=${event.step} ${event.direction} dx=${event.dx} dy=${event.dy}" }
            }
            is CalibrationCompleteEvent -> {
                fireMessage { "calibration completed" }
            }
            is CalibrationDataFlippedEvent -> Unit
            is CalibrationFailedEvent -> {
                fireMessage { "calibration failed. ${event.reason}" }
            }
            ConfigurationChangeEvent -> client.sendCommand(GetPixelScale)
            is GuideParamChangeEvent -> LOG.info("guide param changed: ${event.name} = ${event.value}")
            is GuideStepEvent -> {
                state = State.GUIDING

                fireMessage { "frame=${event.frame} ra=${event.raDuration} ms ${event.raDirection} dec=${event.decDuration} ms ${event.decDirection}" }

                if (listeners.isNotEmpty()) {
                    client.sendCommand(GetStarImage(64))
                        .whenComplete { image, e ->
                            if (image != null) {
                                val decodedImage = image.decodeImage()
                                val imageBase64 = synchronized(STAR_IMAGE_OUTPUT_STREAM) {
                                    ImageIO.write(decodedImage, "PNG", STAR_IMAGE_OUTPUT_STREAM)
                                    "data:image/png;base64,${STAR_IMAGE_OUTPUT_STREAM.base64()}"
                                }
                                val guideStar = GuideStar(lockPosition, image.starPosition, imageBase64, event)
                                listeners.forEach { it.onGuideStep(guideStar) }
                            } else if (e != null) {
                                LOG.error("failed to get star image", e)
                            }
                        }
                }
            }
            is GuidingDitheredEvent -> {
                dither[0] = event.dx
                dither[1] = event.dy
                fireMessage { "dithered. dx=${event.dx} dy=${event.dy}" }
            }
            GuidingStoppedEvent -> Unit
            LockPositionLostEvent -> {
                state = State.LOST_LOCK
                fireMessage { "lock position lost" }
            }
            is LockPositionSetEvent -> {
                lockPosition = event
                fireMessage { "lock position set. x=${event.x} y=${event.y}" }
            }
            LockPositionShiftLimitReachedEvent -> restartForLostShiftLock()
            is LoopingExposuresEvent -> {
                state = State.LOOPING
                fireMessage { "frame: ${event.frame}" }
            }
            LoopingExposuresStoppedEvent -> state = State.STOPPED
            PausedEvent -> state = State.PAUSED
            ResumedEvent -> Unit
            SettleBeginEvent -> Unit
            is SettleDoneEvent -> {
                settling = false
                fireMessage { "settling done" }
            }
            is SettlingEvent -> {
                settling = true
                fireMessage { "settling started" }
            }
            is StarLostEvent -> {
                state = State.LOST_LOCK
                fireMessage { "star lost. status=${event.status}" }
            }
            is StarSelectedEvent -> {
                starPosition = event
                fireMessage { "star selected. x=${event.x} y=${event.y}" }
            }
            is StartCalibrationEvent -> {
                state = State.CALIBRATING
                fireMessage { "calibration started" }
            }
            StartGuidingEvent -> Unit
            is VersionEvent -> Unit
        }
    }

    override fun <T> onCommandProcessed(command: PHD2Command<T>, result: T?, error: String?) {
        if (result != null) {
            if (command is GetPixelScale) {
                pixelScale = result as Double
            }
        } else if (error != null) {
            LOG.error("command error. command={}, message={}", command.methodName, error)
        }
    }

    override fun close() {
        listeners.clear()
        client.unregisterListener(this)
        client.close()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<PHD2Guider>()
        @JvmStatic private val STAR_IMAGE_OUTPUT_STREAM = Base64OutputStream(32 * 1024)
    }
}
