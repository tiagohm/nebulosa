package nebulosa.alpaca.indi.devices.cameras

import nebulosa.alpaca.api.AlpacaCameraService
import nebulosa.alpaca.api.CameraState
import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.alpaca.api.PulseGuideDirection
import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.alpaca.indi.devices.ASCOMDevice
import nebulosa.imaging.algorithms.transformation.CfaPattern
import nebulosa.indi.device.Device
import nebulosa.indi.device.camera.*
import nebulosa.indi.device.guide.GuideOutputPulsingChanged
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.PropertyState
import java.time.Duration
import kotlin.math.max
import kotlin.math.min

data class ASCOMCamera(
    override val device: ConfiguredDevice,
    override val service: AlpacaCameraService,
    override val client: AlpacaClient,
) : ASCOMDevice(), Camera {

    @Volatile override var exposuring = false
        private set
    @Volatile override var hasCoolerControl = false
        private set
    @Volatile override var coolerPower = 0.0
        private set
    @Volatile override var cooler = false
        private set
    @Volatile override var hasDewHeater = false
        private set
    @Volatile override var dewHeater = false
        private set
    @Volatile override var frameFormats = emptyList<String>()
        private set
    @Volatile override var canAbort = false
        private set
    @Volatile override var cfaOffsetX = 0
        private set
    @Volatile override var cfaOffsetY = 0
        private set
    @Volatile override var cfaType = CfaPattern.RGGB
        private set
    @Volatile override var exposureMin: Duration = Duration.ZERO
        private set
    @Volatile override var exposureMax: Duration = Duration.ZERO
        private set
    @Volatile override var exposureState = PropertyState.IDLE
        private set
    @Volatile override var exposureTime: Duration = Duration.ZERO
        private set
    @Volatile override var hasCooler = false
        private set
    @Volatile override var canSetTemperature = false
        private set
    @Volatile override var canSubFrame = true
        private set
    @Volatile override var x = 0
        private set
    @Volatile override var minX = 0
        private set
    @Volatile override var maxX = 0
        private set
    @Volatile override var y = 0
        private set
    @Volatile override var minY = 0
        private set
    @Volatile override var maxY = 0
        private set
    @Volatile override var width = 0
        private set
    @Volatile override var minWidth = 0
        private set
    @Volatile override var maxWidth = 0
        private set
    @Volatile override var height = 0
        private set
    @Volatile override var minHeight = 0
        private set
    @Volatile override var maxHeight = 0
        private set
    @Volatile override var canBin = true
        private set
    @Volatile override var maxBinX = 1
        private set
    @Volatile override var maxBinY = 1
        private set
    @Volatile override var binX = 1
        private set
    @Volatile override var binY = 1
        private set
    @Volatile override var gain = 0
        private set
    @Volatile override var gainMin = 0
        private set
    @Volatile override var gainMax = 0
        private set
    @Volatile override var offset = 0
        private set
    @Volatile override var offsetMin = 0
        private set
    @Volatile override var offsetMax = 0
        private set
    @Volatile override var hasGuiderHead = false // TODO: ASCOM has guider head?
        private set
    @Volatile override var pixelSizeX = 0.0
        private set
    @Volatile override var pixelSizeY = 0.0
        private set

    @Volatile override var hasThermometer = false
        private set
    @Volatile override var temperature = 0.0
        private set

    @Volatile override var canPulseGuide = false
        private set
    @Volatile override var pulseGuiding = false
        private set

    @Volatile private var cameraState = CameraState.IDLE
    @Volatile private var frameType = FrameType.LIGHT

    init {
        refresh(0L)
    }

    override fun cooler(enabled: Boolean) {
        executeRequest(service.cooler(device.number, enabled))
    }

    override fun dewHeater(enabled: Boolean) {
        // TODO
    }

    override fun temperature(value: Double) {
        executeRequest(service.setpointCCDTemperature(device.number, value))
    }

    override fun frameFormat(format: String?) {
        // TODO
    }

    override fun frameType(type: FrameType) {
        frameType = type
    }

    override fun frame(x: Int, y: Int, width: Int, height: Int) {
        executeRequest(service.startX(device.number, x)) ?: return
        executeRequest(service.startY(device.number, y)) ?: return
        executeRequest(service.numX(device.number, width)) ?: return
        executeRequest(service.numY(device.number, height))
    }

    override fun bin(x: Int, y: Int) {
        executeRequest(service.binX(device.number, x)) ?: return
        executeRequest(service.binY(device.number, y))
    }

    override fun gain(value: Int) {
        executeRequest(service.gain(device.number, value))
    }

    override fun offset(value: Int) {
        executeRequest(service.offset(device.number, value))
    }

    override fun startCapture(exposureTime: Duration) {
        this.exposureTime = exposureTime
        executeRequest(service.startExposure(device.number, exposureTime.toNanos() / 1000000000.0, frameType == FrameType.DARK))
    }

    override fun abortCapture() {
        executeRequest(service.abortExposure(device.number))
    }

    private fun pulseGuide(direction: PulseGuideDirection, duration: Duration) {
        val durationInMilliseconds = duration.toMillis()

        executeRequest(service.pulseGuide(device.number, direction, durationInMilliseconds)) ?: return

        if (durationInMilliseconds > 0) {
            pulseGuiding = true
            client.fireOnEventReceived(GuideOutputPulsingChanged(this))
        }
    }

    override fun guideNorth(duration: Duration) {
        pulseGuide(PulseGuideDirection.NORTH, duration)
    }

    override fun guideSouth(duration: Duration) {
        pulseGuide(PulseGuideDirection.SOUTH, duration)
    }

    override fun guideEast(duration: Duration) {
        pulseGuide(PulseGuideDirection.EAST, duration)
    }

    override fun guideWest(duration: Duration) {
        pulseGuide(PulseGuideDirection.WEST, duration)
    }

    override fun sendMessageToServer(message: INDIProtocol) {
    }

    override fun snoop(devices: Iterable<Device?>) {
    }

    override fun handleMessage(message: INDIProtocol) {
    }

    override fun onConnected() {
        processExposureMinMax()
        processFrameMinMax()
        processGainMinMax()
        processOffsetMinMax()
        processCapabilities()
        processPixelSize()
        processCfaOffset()
    }

    override fun onDisconnected() {
    }

    override fun reset() {
        super.reset()

        exposuring = false
        hasCoolerControl = false
        coolerPower = 0.0
        cooler = false
        hasDewHeater = false
        dewHeater = false
        frameFormats = emptyList()
        canAbort = false
        cfaOffsetX = 0
        cfaOffsetY = 0
        cfaType = CfaPattern.RGGB
        exposureMin = Duration.ZERO
        exposureMax = Duration.ZERO
        exposureState = PropertyState.IDLE
        exposureTime = Duration.ZERO
        hasCooler = false
        canSetTemperature = false
        canSubFrame = true
        x = 0
        minX = 0
        maxX = 0
        y = 0
        minY = 0
        maxY = 0
        width = 0
        minWidth = 0
        maxWidth = 0
        height = 0
        minHeight = 0
        maxHeight = 0
        canBin = true
        maxBinX = 1
        maxBinY = 1
        binX = 1
        binY = 1
        gain = 0
        gainMin = 0
        gainMax = 0
        offset = 0
        offsetMin = 0
        offsetMax = 0
        hasGuiderHead = false
        pixelSizeX = 0.0
        pixelSizeY = 0.0
        hasThermometer = false
        temperature = 0.0
        canPulseGuide = false
        pulseGuiding = false
        cameraState = CameraState.IDLE
    }

    override fun close() {
        super.close()
        reset()
    }

    override fun refresh(elapsedTimeInSeconds: Long) {
        super.refresh(elapsedTimeInSeconds)

        executeRequest(service.cameraState(device.number)) { processCameraState(it.value) }

        processBin()
        processGain()
        processOffset()
        processCooler()
    }

    private fun processCameraState(value: CameraState) {
        if (cameraState != value) {
            cameraState = value

            val prevExposuring = exposuring
            val prevExposureState = exposureState

            when (value) {
                CameraState.IDLE -> {
                    if (exposuring) {
                        exposuring = false
                        exposureState = PropertyState.IDLE
                    }
                }
                CameraState.WAITING -> {
                    if (exposuring) {
                        exposuring = false
                        exposureState = PropertyState.BUSY
                    }
                }
                CameraState.EXPOSURING -> {
                    if (!exposuring) {
                        exposuring = true
                        exposureState = PropertyState.BUSY
                    }
                }
                CameraState.READING -> {
                    if (exposuring) {
                        exposuring = false
                        exposureState = PropertyState.OK
                    }
                }
                CameraState.DOWNLOAD -> {
                    if (exposuring) {
                        exposuring = false
                        exposureState = PropertyState.OK
                    }
                }
                CameraState.ERROR -> {
                    if (exposuring) {
                        exposuring = false
                        exposureState = PropertyState.ALERT
                    }
                }
            }

            if (prevExposuring != exposuring) client.fireOnEventReceived(CameraExposuringChanged(this))
            if (prevExposureState != exposureState) client.fireOnEventReceived(CameraExposureStateChanged(this, prevExposureState))

            if (exposuring) {
                executeRequest(service.percentCompleted(device.number)) {

                }
            }

            if (exposureState == PropertyState.IDLE && (prevExposureState == PropertyState.BUSY || exposuring)) {
                client.fireOnEventReceived(CameraExposureAborted(this))
            } else if (exposureState == PropertyState.OK && prevExposureState == PropertyState.BUSY) {
                client.fireOnEventReceived(CameraExposureFinished(this))
            } else if (exposureState == PropertyState.ALERT && prevExposureState != PropertyState.ALERT) {
                client.fireOnEventReceived(CameraExposureFailed(this))
            }
        }
    }

    private fun processBin() {
        executeRequest(service.binX(device.number)) { x ->
            executeRequest(service.binY(device.number)) { y ->
                if (x.value != binX || y.value != binY) {
                    binX = x.value
                    binY = y.value

                    client.fireOnEventReceived(CameraBinChanged(this))
                }
            }
        }
    }

    private fun processGainMinMax() {
        executeRequest(service.gainMin(device.number)) { min ->
            executeRequest(service.gainMax(device.number)) { max ->
                gainMin = min.value
                gainMax = max.value
                gain = max(gainMin, min(gain, gainMax))

                client.fireOnEventReceived(CameraGainMinMaxChanged(this))
            }
        }
    }

    private fun processGain() {
        executeRequest(service.gain(device.number)) {
            if (it.value != gain) {
                gain = it.value

                client.fireOnEventReceived(CameraGainChanged(this))
            }
        }
    }

    private fun processOffsetMinMax() {
        executeRequest(service.offsetMin(device.number)) { min ->
            executeRequest(service.offsetMax(device.number)) { max ->
                offsetMin = min.value
                offsetMax = max.value
                offset = max(offsetMin, min(offset, offsetMax))

                client.fireOnEventReceived(CameraOffsetMinMaxChanged(this))
            }
        }
    }

    private fun processOffset() {
        executeRequest(service.offset(device.number)) {
            if (it.value != offset) {
                offset = it.value

                client.fireOnEventReceived(CameraOffsetChanged(this))
            }
        }
    }

    private fun processFrameMinMax() {
        executeRequest(service.x(device.number)) { w ->
            executeRequest(service.y(device.number)) { h ->
                width = w.value
                height = h.value
                minWidth = 0
                maxWidth = width
                minHeight = 0
                maxHeight = height
                x = 0
                minX = 0
                maxX = width - 1
                y = 0
                minY = 0
                maxY = height - 1

                if (!processFrame()) {
                    client.fireOnEventReceived(CameraFrameChanged(this))
                }
            }
        }
    }

    private fun processFrame(): Boolean {
        executeRequest(service.numX(device.number)) { w ->
            executeRequest(service.numY(device.number)) { h ->
                executeRequest(service.startX(device.number)) { x ->
                    executeRequest(service.startY(device.number)) { y ->
                        if (w.value != width || h.value != height || x.value != this.x || y.value != this.y) {
                            width = w.value
                            height = h.value
                            this.x = x.value
                            this.y = y.value

                            client.fireOnEventReceived(CameraFrameChanged(this))

                            return true
                        }
                    }
                }
            }
        }

        return false
    }

    private fun processCooler() {
        if (hasCoolerControl) {
            executeRequest(service.coolerPower(device.number)) {
                if (coolerPower != it.value) {
                    coolerPower = it.value

                    client.fireOnEventReceived(CameraCoolerPowerChanged(this))
                }
            }
        }

        if (hasCooler) {
            executeRequest(service.isCoolerOn(device.number)) {
                if (cooler != it.value) {
                    cooler = it.value

                    client.fireOnEventReceived(CameraCoolerChanged(this))
                }
            }
        }
    }

    private fun processPixelSize() {
        executeRequest(service.pixelSizeX(device.number)) { x ->
            executeRequest(service.pixelSizeY(device.number)) { y ->
                if (pixelSizeX != x.value || pixelSizeY != y.value) {
                    pixelSizeX = x.value
                    pixelSizeY = y.value

                    client.fireOnEventReceived(CameraPixelSizeChanged(this))
                }
            }
        }
    }

    private fun processCfaOffset() {
        executeRequest(service.bayerOffsetX(device.number)) { x ->
            executeRequest(service.bayerOffsetY(device.number)) { y ->
                if (cfaOffsetX != x.value || cfaOffsetY != y.value) {
                    cfaOffsetX = x.value
                    cfaOffsetY = y.value

                    client.fireOnEventReceived(CameraCfaChanged(this))
                }
            }
        }
    }

    private fun processExposureMinMax() {
        executeRequest(service.exposureMin(device.number)) { min ->
            executeRequest(service.exposureMax(device.number)) { max ->
                exposureMin = Duration.ofNanos((min.value * 1000000000.0).toLong())
                exposureMax = Duration.ofNanos((max.value * 1000000000.0).toLong())

                client.fireOnEventReceived(CameraExposureMinMaxChanged(this))
            }
        }
    }

    private fun processCapabilities() {
        executeRequest(service.canAbortExposure(device.number)) {
            if (it.value != canAbort) {
                canAbort = it.value

                client.fireOnEventReceived(CameraCanAbortChanged(this))
            }
        }

        executeRequest(service.canCoolerPower(device.number)) {
            if (it.value != hasCoolerControl) {
                hasCoolerControl = it.value

                client.fireOnEventReceived(CameraCoolerControlChanged(this))
            }
        }

        executeRequest(service.canPulseGuide(device.number)) {
            if (it.value != canPulseGuide) {
                canPulseGuide = it.value

                client.registerGuideOutput(this)

                LOG.info("guide output attached: {}", name)
            }
        }

        executeRequest(service.canSetCCDTemperature(device.number)) {
            if (it.value != canSetTemperature) {
                canSetTemperature = it.value
                hasCooler = canSetTemperature

                client.fireOnEventReceived(CameraHasCoolerChanged(this))
                client.fireOnEventReceived(CameraCanSetTemperatureChanged(this))
            }
        }
    }

    override fun toString() = "Camera(name=$name, connected=$connected, exposuring=$exposuring," +
        " hasCoolerControl=$hasCoolerControl, cooler=$cooler," +
        " hasDewHeater=$hasDewHeater, dewHeater=$dewHeater," +
        " frameFormats=$frameFormats, canAbort=$canAbort," +
        " cfaOffsetX=$cfaOffsetX, cfaOffsetY=$cfaOffsetY, cfaType=$cfaType," +
        " exposureMin=$exposureMin, exposureMax=$exposureMax," +
        " exposureState=$exposureState, exposureTime=$exposureTime," +
        " hasCooler=$hasCooler, hasThermometer=$hasThermometer, canSetTemperature=$canSetTemperature," +
        " temperature=$temperature, canSubFrame=$canSubFrame," +
        " x=$x, minX=$minX, maxX=$maxX, y=$y, minY=$minY, maxY=$maxY," +
        " width=$width, minWidth=$minWidth, maxWidth=$maxWidth, height=$height," +
        " minHeight=$minHeight, maxHeight=$maxHeight," +
        " canBin=$canBin, maxBinX=$maxBinX, maxBinY=$maxBinY," +
        " binX=$binX, binY=$binY, gain=$gain, gainMin=$gainMin," +
        " gainMax=$gainMax, offset=$offset, offsetMin=$offsetMin," +
        " offsetMax=$offsetMax, hasGuiderHead=$hasGuiderHead," +
        " canPulseGuide=$canPulseGuide, pulseGuiding=$pulseGuiding)"
}
