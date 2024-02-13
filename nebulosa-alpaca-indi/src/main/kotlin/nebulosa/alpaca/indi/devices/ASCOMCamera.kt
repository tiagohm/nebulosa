package nebulosa.alpaca.indi.devices

import nebulosa.alpaca.api.CameraService
import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.imaging.algorithms.transformation.CfaPattern
import nebulosa.indi.device.Device
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.PropertyState
import java.time.Duration

data class ASCOMCamera(
    override val device: ConfiguredDevice,
    override val service: CameraService,
) : ASCOMDevice(), Camera {

    override var exposuring = false
    override var hasCoolerControl = false
    override var coolerPower = 0.0
    override var cooler = false
    override var hasDewHeater = false
    override var dewHeater = false
    override var frameFormats = emptyList<String>()
    override var canAbort = false
    override var cfaOffsetX = 0
    override var cfaOffsetY = 0
    override var cfaType = CfaPattern.RGGB
    override var exposureMin: Duration = Duration.ZERO
    override var exposureMax: Duration = Duration.ZERO
    override var exposureState = PropertyState.IDLE
    override var exposureTime: Duration = Duration.ZERO
    override var hasCooler = false
    override var canSetTemperature = false
    override var canSubFrame = false
    override var x = 0
    override var minX = 0
    override var maxX = 0
    override var y = 0
    override var minY = 0
    override var maxY = 0
    override var width = 0
    override var minWidth = 0
    override var maxWidth = 0
    override var height = 0
    override var minHeight = 0
    override var maxHeight = 0
    override var canBin = false
    override var maxBinX = 1
    override var maxBinY = 1
    override var binX = 1
    override var binY = 1
    override var gain = 0
    override var gainMin = 0
    override var gainMax = 0
    override var offset = 0
    override var offsetMin = 0
    override var offsetMax = 0
    override var hasGuiderHead = false // TODO: ASCOM has guider head?
    override var pixelSizeX = 0.0
    override var pixelSizeY = 0.0

    override var hasThermometer = false
    override var temperature = 0.0

    override var canPulseGuide = false
    override var pulseGuiding = false

    override var connected = false

    override fun cooler(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun dewHeater(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun temperature(value: Double) {
        TODO("Not yet implemented")
    }

    override fun frameFormat(format: String?) {
        TODO("Not yet implemented")
    }

    override fun frameType(type: FrameType) {
        TODO("Not yet implemented")
    }

    override fun frame(x: Int, y: Int, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun bin(x: Int, y: Int) {
        TODO("Not yet implemented")
    }

    override fun gain(value: Int) {
        TODO("Not yet implemented")
    }

    override fun offset(value: Int) {
        TODO("Not yet implemented")
    }

    override fun startCapture(exposureTime: Duration) {
        TODO("Not yet implemented")
    }

    override fun abortCapture() {
        TODO("Not yet implemented")
    }

    override fun guideNorth(duration: Duration) {
        TODO("Not yet implemented")
    }

    override fun guideSouth(duration: Duration) {
        TODO("Not yet implemented")
    }

    override fun guideEast(duration: Duration) {
        TODO("Not yet implemented")
    }

    override fun guideWest(duration: Duration) {
        TODO("Not yet implemented")
    }

    override fun connect() {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }

    override fun sendMessageToServer(message: INDIProtocol) {
        TODO("Not yet implemented")
    }

    override fun snoop(devices: Iterable<Device?>) {
        TODO("Not yet implemented")
    }

    override fun handleMessage(message: INDIProtocol) {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun refresh() {
        TODO("Not yet implemented")
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
