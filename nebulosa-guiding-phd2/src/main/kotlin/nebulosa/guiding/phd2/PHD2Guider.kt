package nebulosa.guiding.phd2

import nebulosa.guiding.GuideListener
import nebulosa.guiding.Guider
import nebulosa.log.loggerFor
import nebulosa.math.arcsec
import nebulosa.math.toArcsec
import nebulosa.phd2.client.PHD2Client
import nebulosa.phd2.client.PHD2EventListener
import nebulosa.phd2.client.commands.*
import nebulosa.phd2.client.events.*
import java.io.Closeable
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

class PHD2Guider(private val client: PHD2Client) : Guider, PHD2EventListener, Closeable {

    private val listeners = hashSetOf<GuideListener>()
    @Volatile private var version = ""
    @Volatile private var ditherDx = 0.0
    @Volatile private var ditherDy = 0.0
    @Volatile private var settling = false
    @Volatile private var pixelScale = 0.0
    @Volatile private var shiftRate = SiderealShiftTrackingRate.DISABLED
    @Volatile private var shiftEnabled = false
    @Volatile private var shiftRateAxis = ShiftAxesType.RADEC

    override var state = State.STOPPED
        private set(value) {
            val prevState = field
            field = value

            if (value != prevState) {
                listeners.forEach { it.onStateChanged(value) }
            }
        }

    init {
        client.registerListener(this)
    }

    override fun registerGuideListener(listener: GuideListener) {
        listeners.add(listener)
    }

    override fun unregisterGuideListener(listener: GuideListener) {
        listeners.remove(listener)
    }

    override var settlePixels = 1.5
    override var settleTime = 10.seconds
    override var settleTimeout = 30.seconds

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
            LOG.info("app is already calibrating. Waiting for calibration to finish")
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

            val guidingHasBegun = waitForGuidingStarted()

            if (guidingHasBegun) {
                if (waitForSettle) {
                    waitForSettling()
                }

                return
            }
        } else {
            LOG.warn("failed to select star")
        }

        Thread.sleep(1000)

        stopGuiding()
    }

    override fun stopGuiding() {
        val state = client.sendCommandSync(GetAppState)

        if (state != State.GUIDING && state != State.CALIBRATING && state != State.LOST_LOCK) {
            LOG.info("stop Guiding skipped, as the app is already in state {}", state)
            return
        }

        client.sendCommandSync(StopCapture)
    }

    override fun autoSelectGuideStar() {
        val state = client.sendCommandSync(GetAppState)

        if (state != State.LOOPING) {
            client.sendCommandSync(Loop)
            Thread.sleep(5000)
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

            client.sendCommandSync(Dither(pixels, raOnly, settlePixels, settleTime, settleTimeout))

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

    private fun waitForGuidingStarted(): Boolean {
        val isGuiding = waitForState(State.GUIDING)
        if (!isGuiding) return false
        settling = true
        return true
    }

    private fun waitForState(state: State): Boolean {
        return try {
            while (true) {
                if (client.sendCommandSync(GetAppState) == state) break
                Thread.sleep(1000)
            }

            true
        } catch (e: Throwable) {
            false
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
            val command = Guide(settlePixels, settleTime, settleTimeout, forceCalibration)
            LOG.info("requesting to start guiding. command={}", command)
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
        val settleTimeout = min(30000L, settleTimeout.inWholeMilliseconds)

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
        when (event) {
            is AlertEvent -> Unit
            is AppStateEvent -> state = event.state
            is CalibratingEvent -> Unit
            is CalibrationCompleteEvent -> Unit
            is CalibrationDataFlippedEvent -> Unit
            is CalibrationFailedEvent -> Unit
            ConfigurationChangeEvent -> client.sendCommandSync(GetPixelScale)
            is GuideParamChangeEvent -> Unit
            is GuideStepEvent -> {
                state = State.GUIDING
                listeners.forEach {
                    it.onGuideStep(
                        event.frame, event.time, event.raDistance, event.decDistance,
                        event.raDirection, event.decDirection, event.raDuration, event.decDuration
                    )
                }
            }
            is GuidingDitheredEvent -> {
                ditherDx = event.dx
                ditherDy = event.dy
            }
            GuidingStoppedEvent -> Unit
            LockPositionLostEvent -> {
                state = State.LOST_LOCK
                LOG.warn("lock position lost")
            }
            is LockPositionSetEvent -> LOG.info("lock position set. x={}, y={}", event.x, event.y)
            LockPositionShiftLimitReachedEvent -> restartForLostShiftLock()
            is LoopingExposuresEvent -> state = State.LOOPING
            LoopingExposuresStoppedEvent -> state = State.STOPPED
            PausedEvent -> state = State.PAUSED
            ResumedEvent -> Unit
            SettleBeginEvent -> Unit
            is SettleDoneEvent -> {
                settling = false
                listeners.forEach { it.onSettleDone(event.error.isNotEmpty()) }
            }
            is SettlingEvent -> {
                settling = true
                listeners.forEach { it.onSettlingStarted() }
                LOG.info("settling started. event={}", event)
            }
            is StarLostEvent -> {
                state = State.LOST_LOCK
                LOG.warn("star lost. status={}", event.status)
            }
            is StarSelectedEvent -> LOG.info("star selected. x={}, y={}", event.x, event.y)
            is StartCalibrationEvent -> state = State.CALIBRATING
            StartGuidingEvent -> Unit
            is VersionEvent -> version = event.version
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
        client.unregisterListener(this)
        client.close()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<PHD2Guider>()
    }
}
