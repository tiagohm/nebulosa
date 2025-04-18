package nebulosa.indi.client.device.camera

import nebulosa.fits.FitsHeaderCard
import nebulosa.image.algorithms.transformation.CfaPattern
import nebulosa.image.format.HeaderCard
import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.INDIDriverInfo
import nebulosa.indi.client.device.INDIDevice
import nebulosa.indi.client.device.handler.INDIGuideOutputHandler
import nebulosa.indi.device.Device
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraBinChanged
import nebulosa.indi.device.camera.CameraCanAbortChanged
import nebulosa.indi.device.camera.CameraCanBinChanged
import nebulosa.indi.device.camera.CameraCanSetTemperatureChanged
import nebulosa.indi.device.camera.CameraCanSubFrameChanged
import nebulosa.indi.device.camera.CameraCfaChanged
import nebulosa.indi.device.camera.CameraCoolerChanged
import nebulosa.indi.device.camera.CameraCoolerControlChanged
import nebulosa.indi.device.camera.CameraCoolerPowerChanged
import nebulosa.indi.device.camera.CameraExposureAborted
import nebulosa.indi.device.camera.CameraExposureFailed
import nebulosa.indi.device.camera.CameraExposureFinished
import nebulosa.indi.device.camera.CameraExposureMinMaxChanged
import nebulosa.indi.device.camera.CameraExposureProgressChanged
import nebulosa.indi.device.camera.CameraExposureStateChanged
import nebulosa.indi.device.camera.CameraExposuringChanged
import nebulosa.indi.device.camera.CameraFrameCaptured
import nebulosa.indi.device.camera.CameraFrameChanged
import nebulosa.indi.device.camera.CameraFrameFormatsChanged
import nebulosa.indi.device.camera.CameraGainChanged
import nebulosa.indi.device.camera.CameraGainMinMaxChanged
import nebulosa.indi.device.camera.CameraHasCoolerChanged
import nebulosa.indi.device.camera.CameraOffsetChanged
import nebulosa.indi.device.camera.CameraOffsetMinMaxChanged
import nebulosa.indi.device.camera.CameraPixelSizeChanged
import nebulosa.indi.device.camera.CameraTemperatureChanged
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.camera.GuideHead
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.protocol.DefBLOBVector
import nebulosa.indi.protocol.DefNumber
import nebulosa.indi.protocol.DefNumberVector
import nebulosa.indi.protocol.DefSwitchVector
import nebulosa.indi.protocol.DefVector.Companion.isNotReadOnly
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberElement
import nebulosa.indi.protocol.NumberVector
import nebulosa.indi.protocol.PropertyState
import nebulosa.indi.protocol.SetBLOBVector
import nebulosa.indi.protocol.SwitchVector
import nebulosa.indi.protocol.TextVector
import nebulosa.io.Base64InputStream
import nebulosa.log.loggerFor
import java.time.Duration

// https://github.com/indilib/indi/blob/master/libs/indibase/indiccd.cpp

internal open class INDICamera(
    final override val sender: INDIClient,
    final override val driver: INDIDriverInfo,
) : INDIDevice(), Camera {

    @Volatile final override var exposuring = false
    @Volatile final override var hasCoolerControl = false
    @Volatile final override var coolerPower = 0.0
    @Volatile final override var cooler = false
    @Volatile final override var hasDewHeater = false
    @Volatile final override var dewHeater = false
    @Volatile final override var frameFormats = emptyList<String>()
    @Volatile final override var canAbort = false
    @Volatile final override var cfaOffsetX = 0
    @Volatile final override var cfaOffsetY = 0
    @Volatile final override var cfaType = CfaPattern.RGGB
    @Volatile final override var exposureMin = 0L
    @Volatile final override var exposureMax = 0L
    @Volatile final override var exposureState = PropertyState.IDLE
    @Volatile final override var exposureTime = 0L
    @Volatile final override var hasCooler = false
    @Volatile final override var canSetTemperature = false
    @Volatile final override var canSubFrame = false
    @Volatile final override var x = 0
    @Volatile final override var minX = 0
    @Volatile final override var maxX = 0
    @Volatile final override var y = 0
    @Volatile final override var minY = 0
    @Volatile final override var maxY = 0
    @Volatile final override var width = 0
    @Volatile final override var minWidth = 0
    @Volatile final override var maxWidth = 0
    @Volatile final override var height = 0
    @Volatile final override var minHeight = 0
    @Volatile final override var maxHeight = 0
    @Volatile final override var canBin = false
    @Volatile final override var maxBinX = 1
    @Volatile final override var maxBinY = 1
    @Volatile final override var binX = 1
    @Volatile final override var binY = 1
    @Volatile final override var gain = 0
    @Volatile final override var gainMin = 0
    @Volatile final override var gainMax = 0
    @Volatile final override var offset = 0
    @Volatile final override var offsetMin = 0
    @Volatile final override var offsetMax = 0
    @Volatile final override var pixelSizeX = 0.0
    @Volatile final override var pixelSizeY = 0.0

    @Volatile final override var hasThermometer = false
    @Volatile final override var temperature = 0.0

    @Suppress("LeakingThis")
    private val guideOutput = INDIGuideOutputHandler(this)

    final override val canPulseGuide
        get() = guideOutput.canPulseGuide

    final override val pulseGuiding
        get() = guideOutput.pulseGuiding

    final override var guideHead: GuideHeadCamera? = null
        private set

    override fun handleMessage(message: INDIProtocol) {
        val isGuider = message.name.isNotEmpty() && message.name[0] == 'G'
        val isGuideHead = this is GuideHead

        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "CCD_COOLER" -> {
                        if (message is DefSwitchVector) {
                            hasCoolerControl = true
                            sender.fireOnEventReceived(CameraCoolerControlChanged(this))
                        }

                        cooler = message["COOLER_ON"]?.value == true
                        sender.fireOnEventReceived(CameraCoolerChanged(this))
                    }
                    "CCD_CAPTURE_FORMAT" -> {
                        if (message is DefSwitchVector && message.isNotEmpty()) {
                            frameFormats = message.map { it.name }
                            sender.fireOnEventReceived(CameraFrameFormatsChanged(this))
                        }
                    }
                    "CCD_ABORT_EXPOSURE",
                    "GUIDER_ABORT_EXPOSURE" -> {
                        if (isGuider == isGuideHead && message is DefSwitchVector) {
                            canAbort = message.isNotReadOnly
                            sender.fireOnEventReceived(CameraCanAbortChanged(this))
                        }
                    }
                }
            }
            is TextVector<*> -> {
                when (message.name) {
                    "CCD_CFA" -> {
                        cfaOffsetX = message["CFA_OFFSET_X"]!!.value.toInt()
                        cfaOffsetY = message["CFA_OFFSET_Y"]!!.value.toInt()
                        cfaType = CfaPattern.valueOf(message["CFA_TYPE"]!!.value)
                        sender.fireOnEventReceived(CameraCfaChanged(this))
                    }
                }
            }
            is NumberVector<*> -> {
                when (message.name) {
                    "CCD_INFO",
                    "GUIDER_INFO" -> {
                        if (isGuider == isGuideHead) {
                            pixelSizeX = message["CCD_PIXEL_SIZE_X"]?.value ?: 0.0
                            pixelSizeY = message["CCD_PIXEL_SIZE_Y"]?.value ?: 0.0

                            sender.fireOnEventReceived(CameraPixelSizeChanged(this))
                        }
                    }
                    "CCD_EXPOSURE",
                    "GUIDER_EXPOSURE" -> {
                        if (isGuider == isGuideHead) {
                            val element = message[if (isGuider) "GUIDER_EXPOSURE_VALUE" else "CCD_EXPOSURE_VALUE"]!!

                            if (element is DefNumber) {
                                exposureMin = (element.min * MICROS_TO_SECONDS).toLong()
                                exposureMax = (element.max * MICROS_TO_SECONDS).toLong()

                                sender.fireOnEventReceived(CameraExposureMinMaxChanged(this))
                            }

                            val prevExposureState = exposureState
                            exposureState = message.state

                            if (exposureState == PropertyState.BUSY || exposureState == PropertyState.OK) {
                                exposureTime = (element.value * MICROS_TO_SECONDS).toLong()

                                sender.fireOnEventReceived(CameraExposureProgressChanged(this))
                            }

                            val prevIsExposuring = exposuring
                            exposuring = exposureState == PropertyState.BUSY

                            if (prevIsExposuring != exposuring) {
                                sender.fireOnEventReceived(CameraExposuringChanged(this))
                            }

                            if (exposureState == PropertyState.IDLE && (prevExposureState == PropertyState.BUSY || exposuring)) {
                                sender.fireOnEventReceived(CameraExposureAborted(this))
                            } else if (exposureState == PropertyState.OK && prevExposureState == PropertyState.BUSY) {
                                sender.fireOnEventReceived(CameraExposureFinished(this))
                            } else if (exposureState == PropertyState.ALERT) {
                                sender.fireOnEventReceived(CameraExposureFailed(this))
                            }

                            if (prevExposureState != exposureState) {
                                sender.fireOnEventReceived(CameraExposureStateChanged(this))
                            }
                        }
                    }
                    "CCD_COOLER_POWER" -> {
                        message.first().value.also {
                            if (it != coolerPower) {
                                coolerPower = it
                                sender.fireOnEventReceived(CameraCoolerPowerChanged(this))
                            }
                        }
                    }
                    "CCD_TEMPERATURE" -> {
                        if (message is DefNumberVector) {
                            hasCooler = true
                            canSetTemperature = message.isNotReadOnly

                            sender.fireOnEventReceived(CameraHasCoolerChanged(this))
                            sender.fireOnEventReceived(CameraCanSetTemperatureChanged(this))

                            if (!hasThermometer && !isGuideHead) {
                                hasThermometer = true
                                sender.registerThermometer(this)
                            }
                        }

                        temperature = message["CCD_TEMPERATURE_VALUE"]!!.value
                        sender.fireOnEventReceived(CameraTemperatureChanged(this))
                    }
                    "CCD_FRAME",
                    "GUIDER_FRAME" -> {
                        if (isGuider == isGuideHead) {
                            if (message is DefNumberVector) {
                                canSubFrame = message.isNotReadOnly
                                sender.fireOnEventReceived(CameraCanSubFrameChanged(this))

                                minX = message["X"]!!.min.toInt()
                                maxX = message["X"]!!.max.toInt()
                                minY = message["Y"]!!.min.toInt()
                                maxY = message["Y"]!!.max.toInt()
                                minWidth = message["WIDTH"]!!.min.toInt()
                                maxWidth = message["WIDTH"]!!.max.toInt()
                                minHeight = message["HEIGHT"]!!.min.toInt()
                                maxHeight = message["HEIGHT"]!!.max.toInt()
                            }

                            x = message["X"]!!.value.toInt()
                            y = message["Y"]!!.value.toInt()
                            width = message["WIDTH"]!!.value.toInt()
                            height = message["HEIGHT"]!!.value.toInt()

                            sender.fireOnEventReceived(CameraFrameChanged(this))
                        }
                    }
                    "CCD_BINNING",
                    "GUIDER_BINNING" -> {
                        if (isGuider == isGuideHead) {
                            if (message is DefNumberVector) {
                                canBin = message.isNotReadOnly
                                maxBinX = message["HOR_BIN"]!!.max.toInt()
                                maxBinY = message["VER_BIN"]!!.max.toInt()
                                sender.fireOnEventReceived(CameraCanBinChanged(this))
                            }

                            binX = message["HOR_BIN"]!!.value.toInt()
                            binY = message["VER_BIN"]!!.value.toInt()
                            sender.fireOnEventReceived(CameraBinChanged(this))
                        }
                    }
                }
            }
            is DefBLOBVector -> {
                if (!isGuideHead && message.name == "CCD2" && guideHead == null) {
                    guideHead = GuideHeadCamera(this)
                    sender.registerGuideHead(guideHead!!)
                    return
                }
            }
            is SetBLOBVector -> {
                // Main camera handles both messages.
                if (!isGuideHead) {
                    val ccd = message[message.name]!!

                    if (ccd.format.contains("fits", true)) {
                        val compressed = COMPRESSION_FORMATS.any { ccd.format.endsWith(it, true) }

                        if (!compressed) {
                            val stream = Base64InputStream(ccd.value)
                            val camera = if (message.name == "CCD2") guideHead!! else this
                            sender.fireOnEventReceived(CameraFrameCaptured(camera, stream = stream))
                        } else {
                            LOG.warn("compressed FITS is not supported yet")
                        }
                    }

                    return
                }
            }
            else -> Unit
        }

        guideOutput.handleMessage(message)
        super.handleMessage(message)
        guideHead?.handleMessage(message)
    }

    override fun cooler(enabled: Boolean) {
        if (hasCoolerControl && cooler != enabled) {
            sendNewSwitch("CCD_COOLER", (if (enabled) "COOLER_ON" else "COOLER_OFF") to true)
        }
    }

    override fun dewHeater(enabled: Boolean) = Unit

    override fun temperature(value: Double) {
        if (canSetTemperature) {
            sendNewNumber("CCD_TEMPERATURE", "CCD_TEMPERATURE_VALUE" to value)
        }
    }

    override fun frameFormat(format: String?) {
        if (!format.isNullOrBlank()) {
            sendNewSwitch("CCD_CAPTURE_FORMAT", format to true)
        }
    }

    override fun frameType(type: FrameType) {
        val name = if (this is GuideHead) "GUIDER_FRAME_TYPE" else "CCD_FRAME_TYPE"
        sendNewSwitch(name, "FRAME_$type" to true)
    }

    override fun frame(x: Int, y: Int, width: Int, height: Int) {
        if (canSubFrame) {
            sendNewNumber(
                "CCD_FRAME", "X" to x.toDouble(), "Y" to y.toDouble(),
                "WIDTH" to width.toDouble(), "HEIGHT" to height.toDouble(),
            )
        }
    }

    override fun bin(x: Int, y: Int) {
        sendNewNumber("CCD_BINNING", "HOR_BIN" to x.toDouble(), "VER_BIN" to y.toDouble())
    }

    override fun gain(value: Int) = Unit

    override fun offset(value: Int) = Unit

    override fun startCapture(exposureTime: Long) {
        sendNewSwitch("CCD_TRANSFER_FORMAT", "FORMAT_FITS" to true)

        if (exposureState != PropertyState.IDLE) {
            exposureState = PropertyState.IDLE
            sender.fireOnEventReceived(CameraExposureStateChanged(this))
        }

        val exposureInSeconds = exposureTime.toDouble() / MICROS_TO_SECONDS

        if (this is GuideHead) {
            sendNewNumber("GUIDER_EXPOSURE", "GUIDER_EXPOSURE_VALUE" to exposureInSeconds)
        } else {
            sendNewNumber("CCD_EXPOSURE", "CCD_EXPOSURE_VALUE" to exposureInSeconds)
        }
    }

    override fun abortCapture() {
        if (canAbort) {
            val name = if (this is GuideHead) "GUIDER_ABORT_EXPOSURE" else "CCD_ABORT_EXPOSURE"
            sendNewSwitch(name, "ABORT" to true)
        }
    }

    override fun guideNorth(duration: Duration) {
        guideOutput.guideNorth(duration)
    }

    override fun guideSouth(duration: Duration) {
        guideOutput.guideSouth(duration)
    }

    override fun guideEast(duration: Duration) {
        guideOutput.guideEast(duration)
    }

    override fun guideWest(duration: Duration) {
        guideOutput.guideWest(duration)
    }

    override fun snoop(devices: Iterable<Device?>) {
        super.snoop(devices)

        val telescope = devices.firstOrNull { it is Mount }?.name ?: ""
        val focuser = devices.firstOrNull { it is Focuser }?.name ?: ""
        val filter = devices.firstOrNull { it is FilterWheel }?.name ?: ""
        val rotator = devices.firstOrNull { it is Rotator }?.name ?: ""

        sendNewText(
            "ACTIVE_DEVICES",
            "ACTIVE_TELESCOPE" to telescope, "ACTIVE_ROTATOR" to rotator,
            "ACTIVE_FOCUSER" to focuser, "ACTIVE_FILTER" to filter,
        )
    }

    private fun sendFITSKeyword(card: HeaderCard) {
        sendNewText("FITS_HEADER", "KEYWORD_NAME" to card.key, "KEYWORD_VALUE" to card.value, "KEYWORD_COMMENT" to card.comment)
    }

    override fun fitsKeywords(vararg cards: HeaderCard) {
        sendFITSKeyword(INDI_CLEAR)
        cards.forEach(::sendFITSKeyword)
    }

    override fun close() {
        if (hasThermometer) {
            hasThermometer = false
            sender.unregisterThermometer(this)
        }

        if (guideHead != null) {
            guideHead?.also(sender::unregisterGuiderHead)
            guideHead = null
        }
    }

    protected fun processGain(message: NumberVector<*>, element: NumberElement) {
        if (message is DefNumberVector) {
            gainMin = element.min.toInt()
            gainMax = element.max.toInt()

            sender.fireOnEventReceived(CameraGainMinMaxChanged(this))
        }

        gain = element.value.toInt()

        sender.fireOnEventReceived(CameraGainChanged(this))
    }

    protected fun processOffset(message: NumberVector<*>, element: NumberElement) {
        if (message is DefNumberVector) {
            offsetMin = element.min.toInt()
            offsetMax = element.max.toInt()

            sender.fireOnEventReceived(CameraOffsetMinMaxChanged(this))
        }

        offset = element.value.toInt()

        sender.fireOnEventReceived(CameraOffsetChanged(this))
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
            " offsetMax=$offsetMax, canPulseGuide=$canPulseGuide, pulseGuiding=$pulseGuiding)"

    internal data class GuideHeadCamera(override val main: INDICamera) : GuideHead, INDICamera(main.sender, main.driver) {

        override val name = main.name + " $GUIDE_HEAD_SUFFIX"

        init {
            exposuring = main.exposuring
            hasCoolerControl = main.hasCoolerControl
            coolerPower = main.coolerPower
            cooler = main.cooler
            hasDewHeater = main.hasDewHeater
            dewHeater = main.dewHeater
            frameFormats = main.frameFormats
            canAbort = main.canAbort
            cfaOffsetX = main.cfaOffsetX
            cfaOffsetY = main.cfaOffsetY
            cfaType = main.cfaType
            exposureMin = main.exposureMin
            exposureMax = main.exposureMax
            exposureState = main.exposureState
            exposureTime = main.exposureTime
            hasCooler = main.hasCooler
            canSetTemperature = main.canSetTemperature
            canSubFrame = main.canSubFrame
            x = main.x
            minX = main.minX
            maxX = main.maxX
            y = main.y
            minY = main.minY
            maxY = main.maxY
            width = main.width
            minWidth = main.minWidth
            maxWidth = main.maxWidth
            height = main.height
            minHeight = main.minHeight
            maxHeight = main.maxHeight
            canBin = main.canBin
            maxBinX = main.maxBinX
            maxBinY = main.maxBinY
            binX = main.binX
            binY = main.binY
            gain = main.gain
            gainMin = main.gainMin
            gainMax = main.gainMax
            offset = main.offset
            offsetMin = main.offsetMin
            offsetMax = main.offsetMax
            pixelSizeX = main.pixelSizeX
            pixelSizeY = main.pixelSizeY
        }

        override fun toString() = "GuideHead(guideHead=${super.toString()}, main=$main)"
    }

    companion object {

        private const val GUIDE_HEAD_SUFFIX = "(Guide Head)"
        private const val MICROS_TO_SECONDS = 1_000_000L

        private val COMPRESSION_FORMATS = arrayOf(".fz", ".gz")
        private val LOG = loggerFor<INDICamera>()
        private val INDI_CLEAR = FitsHeaderCard.create("INDI_CLEAR", "", "")
    }
}
