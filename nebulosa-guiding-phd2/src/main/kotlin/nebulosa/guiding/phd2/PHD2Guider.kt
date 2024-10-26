package nebulosa.guiding.phd2

import nebulosa.guiding.*
import nebulosa.log.*
import nebulosa.math.arcsec
import nebulosa.math.toArcsec
import nebulosa.phd2.client.PHD2Client
import nebulosa.phd2.client.PHD2EventListener
import nebulosa.phd2.client.commands.*
import nebulosa.phd2.client.events.*
import nebulosa.util.concurrency.latch.CountUpDownLatch

class PHD2Guider(private val client: PHD2Client) : Guider, PHD2EventListener {

    private val dither = DoubleArray(2)
    private val settling = CountUpDownLatch()
    @Volatile private var shiftRate = SiderealShiftTrackingRate.DISABLED
    @Volatile private var shiftEnabled = false
    @Volatile private var shiftRateAxis = ShiftAxesType.RADEC
    @Volatile private var lockPosition = GuidePoint.ZERO
    @Volatile private var starPosition = GuidePoint.ZERO
    private val listeners = LinkedHashSet<GuiderListener>()

    override var pixelScale = 1.0
        private set

    override var state = GuideState.STOPPED
        private set(value) {
            if (value != field) {
                field = value
                listeners.forEach { it.onStateChanged(value, pixelScale) }
            }
        }

    override val isSettling
        get() = !settling.get()

    init {
        client.registerListener(this)
    }

    override val canDither = true

    override var settleAmount = Guider.DEFAULT_SETTLE_AMOUNT

    override var settleTime = Guider.DEFAULT_SETTLE_TIME

    override var settleTimeout = Guider.DEFAULT_SETTLE_TIMEOUT

    override fun registerGuiderListener(listener: GuiderListener) {
        listeners.add(listener)
    }

    override fun unregisterGuiderListener(listener: GuiderListener) {
        listeners.remove(listener)
    }

    private inline fun fireMessage(lazyMessage: () -> String) {
        if (listeners.isNotEmpty()) {
            val message = lazyMessage()
            listeners.forEach { it.onMessageReceived(message) }
        }
    }

    override fun startLooping(autoSelectGuideStar: Boolean) {
        val state = client.sendCommandSync(GetAppState)

        if (state == GuideState.STOPPED) {
            if (autoSelectGuideStar) {
                autoSelectGuideStar()
            } else {
                client.sendCommandSync(Loop)
            }
        } else if (state == GuideState.LOOPING) {
            if (autoSelectGuideStar) {
                autoSelectGuideStar()
            }
        }
    }

    override fun startGuiding(forceCalibration: Boolean, waitForSettle: Boolean) {
        val state = client.sendCommandSync(GetAppState)

        if (state == GuideState.GUIDING) {
            LOG.di("app is already guiding. skipping start guiding")
            return
        }

        if (state == GuideState.LOST_LOCK) {
            LOG.di("app lost guide star and needs to stop before starting guiding again")
            stopGuiding()
        }

        if (state == GuideState.CALIBRATING) {
            LOG.di("app is already calibrating. waiting for calibration to finish")
            waitForCalibrationFinished()
        }

        val isCalibrated = forceCalibration || client.sendCommandSync(GetCalibrated)

        startGuide(forceCalibration)
        val starSelected = waitForStarSelected()

        if (starSelected != null) {
            if (!isCalibrated) {
                waitForCalibrationFinished()
            }

            waitForGuidingStarted()

            if (waitForSettle) {
                waitForSettle()
            }

            return
        } else {
            fireMessage { "failed to select star" }
        }

        stopGuiding()
    }

    override fun stopGuiding(force: Boolean) {
        if (!force) {
            val state = client.sendCommandSync(GetAppState)

            if (state != GuideState.GUIDING && state != GuideState.CALIBRATING && state != GuideState.LOST_LOCK) {
                fireMessage { "stop guiding skipped, as the app is already in state $state" }
                return
            }
        }

        client.sendCommandSync(StopCapture)
    }

    override fun autoSelectGuideStar() {
        val state = client.sendCommandSync(GetAppState)

        if (state != GuideState.LOOPING) {
            client.sendCommandSync(Loop)
            waitForState(GuideState.LOOPING)
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

    override fun dither(amount: Double, raOnly: Boolean) {
        val state = client.sendCommandSync(GetAppState)

        if (state == GuideState.GUIDING) {
            waitForSettle()

            val dither = Dither(amount, raOnly, settleAmount, settleTime, settleTimeout)
            client.sendCommandSync(dither)

            settling.countUp()
            waitForSettle()
        }
    }

    private fun waitForCalibrationFinished() {
        while (true) {
            val state = client.sendCommandSync(GetAppState)
            if (state != GuideState.CALIBRATING) break
            Thread.sleep(1000)
        }
    }

    private fun waitForGuidingStarted() {
        waitForState(GuideState.GUIDING)
        settling.countUp()
    }

    private fun waitForState(state: GuideState) {
        while (true) {
            if (client.sendCommandSync(GetAppState) == state) break
            Thread.sleep(1000)
        }
    }

    private fun waitForStarSelected(): IntArray? {
        repeat(5) {
            try {
                return client.sendCommandSync(GetLockPosition, 5)
            } catch (_: Throwable) {
                Thread.sleep(5000)
            }
        }

        return null
    }

    private fun startGuide(forceCalibration: Boolean): Boolean {
        return try {
            waitForSettle()
            val command = Guide(settleAmount, settleTime, settleTimeout, forceCalibration)
            client.sendCommandSync(command)
            refreshShiftLockParams()
            true
        } catch (_: Throwable) {
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

            LOG.di("shift lock params refreshed. rate={}, axes={}", shiftRate, shiftRateAxis)
        } else {
            shiftEnabled = false
        }
    }

    override fun waitForSettle() {
        try {
            settling.await(settleTimeout)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            LOG.dw("PHD2 did not send SettleDone message in expected time")
        } catch (e: Throwable) {
            LOG.e("an error occurrs while waiting for settle done", e)
        } finally {
            settling.reset()
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

        LOG.di("setting shift rate. ra={}, dec={}", raArcsecPerHour, decArcsecPerHour)

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
        LOG.d("event received: {}", event)

        when (event) {
            is AlertEvent -> Unit
            is AppStateEvent -> state = event.state
            is CalibratingEvent -> {
                fireMessage { "${event.state}. step=${event.step} ${event.direction} dx=${event.dx} dy=${event.dy}" }
            }
            is CalibrationCompleteEvent -> {
                client.sendCommand(GetPixelScale)
                fireMessage { "calibration completed" }
            }
            is CalibrationDataFlippedEvent -> Unit
            is CalibrationFailedEvent -> {
                fireMessage { "calibration failed. ${event.reason}" }
            }
            ConfigurationChangeEvent -> client.sendCommand(GetPixelScale)
            is GuideParamChangeEvent -> LOG.di("guide param changed: {} = {}", event.name, event.value)
            is GuideStepEvent -> {
                state = GuideState.GUIDING

                fireMessage { "frame=${event.frame} RA=${event.raDuration} ms ${event.raDirection} DEC=${event.decDuration} ms ${event.decDirection}" }

                if (listeners.isNotEmpty()) {
                    val guideStar = GuideStar(lockPosition, starPosition, "", event)
                    listeners.forEach { it.onGuideStepped(guideStar) }
                }
            }
            is GuidingDitheredEvent -> {
                dither[0] = event.dx
                dither[1] = event.dy
                fireMessage { "dithered. dx=${event.dx} dy=${event.dy}" }
                listeners.forEach { it.onDithered(event.dx, event.dy) }
            }
            GuidingStoppedEvent -> fireMessage { "guiding stopped" }
            LockPositionLostEvent -> {
                state = GuideState.LOST_LOCK
                fireMessage { "lock position lost" }
            }
            is LockPositionSetEvent -> {
                lockPosition = event
                fireMessage { "lock position set. x=${event.x} y=${event.y}" }
            }
            LockPositionShiftLimitReachedEvent -> restartForLostShiftLock()
            is LoopingExposuresEvent -> {
                state = GuideState.LOOPING
                fireMessage { "frame: ${event.frame}" }
            }
            LoopingExposuresStoppedEvent -> state = GuideState.STOPPED
            PausedEvent -> state = GuideState.PAUSED
            ResumedEvent -> Unit
            SettleBeginEvent -> {
                fireMessage { "settling started" }
                listeners.forEach { it.onSettleStarted() }
            }
            is SettleDoneEvent -> {
                settling.reset()
                if (event.error.isEmpty()) fireMessage { "settling done" }
                else fireMessage { event.error }
                listeners.forEach { it.onSettleDone(event.error.ifBlank { null }) }
            }
            is SettlingEvent -> {
                settling.countUp()
                fireMessage { "settling. distance=${event.distance}" }
            }
            is StarLostEvent -> {
                state = GuideState.LOST_LOCK
                fireMessage { "star lost. status=${event.status}" }
            }
            is StarSelectedEvent -> {
                starPosition = event
                fireMessage { "star selected. x=${event.x} y=${event.y}" }
            }
            is StartCalibrationEvent -> {
                state = GuideState.CALIBRATING
                fireMessage { "calibration started" }
            }
            StartGuidingEvent -> fireMessage { "guiding started" }
            is VersionEvent -> Unit
        }
    }

    override fun <T> onCommandProcessed(command: PHD2Command<T>, result: T?, error: String?) {
        if (result != null) {
            if (command is GetPixelScale) {
                pixelScale = result as Double
                LOG.d("pixel scale: {}", pixelScale)
                listeners.forEach { it.onStateChanged(state, pixelScale) }
            }
        } else if (error != null) {
            LOG.e("command error. command={}, message={}", command.methodName, error)
        }
    }

    override fun close() {
        listeners.clear()
        client.unregisterListener(this)
        client.close()
    }

    companion object {

        private val LOG = loggerFor<PHD2Guider>()
    }
}
