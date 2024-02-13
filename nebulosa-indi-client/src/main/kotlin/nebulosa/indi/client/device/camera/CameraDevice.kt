package nebulosa.indi.client.device.camera

import nebulosa.imaging.algorithms.transformation.CfaPattern
import nebulosa.indi.client.device.DeviceProtocolHandler
import nebulosa.indi.client.device.INDIDevice
import nebulosa.indi.device.camera.*
import nebulosa.indi.device.guide.GuideOutputPulsingChanged
import nebulosa.indi.protocol.*
import nebulosa.io.Base64InputStream
import nebulosa.log.loggerFor
import java.time.Duration

internal open class CameraDevice(
    handler: DeviceProtocolHandler,
    name: String,
) : INDIDevice(handler, name), Camera {

    @Volatile final override var exposuring = false
        private set
    @Volatile final override var hasCoolerControl = false
        private set
    @Volatile final override var coolerPower = 0.0
        private set
    @Volatile final override var cooler = false
        private set
    @Volatile final override var hasDewHeater = false
        private set
    @Volatile final override var dewHeater = false
        private set
    @Volatile final override var frameFormats = emptyList<String>()
        private set
    @Volatile final override var canAbort = false
        private set
    @Volatile final override var cfaOffsetX = 0
        private set
    @Volatile final override var cfaOffsetY = 0
        private set
    @Volatile final override var cfaType = CfaPattern.RGGB
        private set
    @Volatile final override var exposureMin: Duration = Duration.ZERO
        private set
    @Volatile final override var exposureMax: Duration = Duration.ZERO
        private set
    @Volatile final override var exposureState = PropertyState.IDLE
        private set
    @Volatile final override var exposureTime: Duration = Duration.ZERO
        private set
    @Volatile final override var hasCooler = false
        private set
    @Volatile final override var canSetTemperature = false
        private set
    @Volatile final override var canSubFrame = false
        private set
    @Volatile final override var x = 0
        private set
    @Volatile final override var minX = 0
        private set
    @Volatile final override var maxX = 0
        private set
    @Volatile final override var y = 0
        private set
    @Volatile final override var minY = 0
        private set
    @Volatile final override var maxY = 0
        private set
    @Volatile final override var width = 0
        private set
    @Volatile final override var minWidth = 0
        private set
    @Volatile final override var maxWidth = 0
        private set
    @Volatile final override var height = 0
        private set
    @Volatile final override var minHeight = 0
        private set
    @Volatile final override var maxHeight = 0
        private set
    @Volatile final override var canBin = false
        private set
    @Volatile final override var maxBinX = 1
        private set
    @Volatile final override var maxBinY = 1
        private set
    @Volatile final override var binX = 1
        private set
    @Volatile final override var binY = 1
        private set
    @Volatile final override var gain = 0
        private set
    @Volatile final override var gainMin = 0
        private set
    @Volatile final override var gainMax = 0
        private set
    @Volatile final override var offset = 0
        private set
    @Volatile final override var offsetMin = 0
        private set
    @Volatile final override var offsetMax = 0
        private set
    @Volatile final override var hasGuiderHead = false // TODO: Handle guider head.
        private set
    @Volatile final override var pixelSizeX = 0.0
        private set
    @Volatile final override var pixelSizeY = 0.0
        private set

    @Volatile final override var hasThermometer = false
        private set
    @Volatile final override var temperature = 0.0
        private set

    @Volatile final override var canPulseGuide = false
        private set
    @Volatile final override var pulseGuiding = false
        private set

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "CCD_COOLER" -> {
                        if (message is DefSwitchVector) {
                            hasCoolerControl = true

                            handler.fireOnEventReceived(CameraCoolerControlChanged(this))
                        }

                        cooler = message["COOLER_ON"]?.value ?: false

                        handler.fireOnEventReceived(CameraCoolerChanged(this))
                    }
                    "CCD_CAPTURE_FORMAT" -> {
                        if (message is DefSwitchVector && message.isNotEmpty()) {
                            frameFormats = message.map { it.name }
                            handler.fireOnEventReceived(CameraFrameFormatsChanged(this))
                        }
                    }
                    "CCD_ABORT_EXPOSURE" -> {
                        if (message is DefSwitchVector) {
                            canAbort = message.isNotReadOnly
                            handler.fireOnEventReceived(CameraCanAbortChanged(this))
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
                        handler.fireOnEventReceived(CameraCfaChanged(this))
                    }
                }
            }
            is NumberVector<*> -> {
                when (message.name) {
                    "CCD_INFO" -> {
                        pixelSizeX = message["CCD_PIXEL_SIZE_X"]?.value ?: 0.0
                        pixelSizeY = message["CCD_PIXEL_SIZE_Y"]?.value ?: 0.0

                        handler.fireOnEventReceived(CameraPixelSizeChanged(this))
                    }
                    "CCD_EXPOSURE" -> {
                        val element = message["CCD_EXPOSURE_VALUE"]!!

                        if (element is DefNumber) {
                            exposureMin = Duration.ofNanos((element.min * 1000000000.0).toLong())
                            exposureMax = Duration.ofNanos((element.max * 1000000000.0).toLong())
                            handler.fireOnEventReceived(CameraExposureMinMaxChanged(this))
                        }

                        val prevExposureState = exposureState
                        exposureState = message.state

                        if (exposureState == PropertyState.BUSY || exposureState == PropertyState.OK) {
                            exposureTime = Duration.ofNanos((element.value * 1000000000.0).toLong())

                            handler.fireOnEventReceived(CameraExposureProgressChanged(this))
                        }

                        val prevIsExposuring = exposuring
                        exposuring = exposureState == PropertyState.BUSY

                        if (prevIsExposuring != exposuring) {
                            handler.fireOnEventReceived(CameraExposuringChanged(this))
                        }

                        if (exposureState == PropertyState.IDLE && (prevExposureState == PropertyState.BUSY || exposuring)) {
                            handler.fireOnEventReceived(CameraExposureAborted(this))
                        } else if (exposureState == PropertyState.OK && prevExposureState == PropertyState.BUSY) {
                            handler.fireOnEventReceived(CameraExposureFinished(this))
                        } else if (exposureState == PropertyState.ALERT && prevExposureState != PropertyState.ALERT) {
                            handler.fireOnEventReceived(CameraExposureFailed(this))
                        }

                        if (prevExposureState != exposureState) {
                            handler.fireOnEventReceived(CameraExposureStateChanged(this, prevExposureState))
                        }
                    }
                    "CCD_COOLER_POWER" -> {
                        coolerPower = message.first().value
                        handler.fireOnEventReceived(CameraCoolerPowerChanged(this))
                    }
                    "CCD_TEMPERATURE" -> {
                        if (message is DefNumberVector) {
                            hasCooler = true
                            canSetTemperature = message.isNotReadOnly

                            handler.fireOnEventReceived(CameraHasCoolerChanged(this))
                            handler.fireOnEventReceived(CameraCanSetTemperatureChanged(this))

                            if (!hasThermometer) {
                                hasThermometer = true
                                handler.registerThermometer(this)
                            }
                        }

                        temperature = message["CCD_TEMPERATURE_VALUE"]!!.value
                        handler.fireOnEventReceived(CameraTemperatureChanged(this))
                    }
                    "CCD_FRAME" -> {
                        if (message is DefNumberVector) {
                            canSubFrame = message.isNotReadOnly
                            handler.fireOnEventReceived(CameraCanSubFrameChanged(this))

                            val minX = message["X"]!!.min.toInt()
                            val maxX = message["X"]!!.max.toInt()
                            val minY = message["Y"]!!.min.toInt()
                            val maxY = message["Y"]!!.max.toInt()
                            val minWidth = message["WIDTH"]!!.min.toInt()
                            val maxWidth = message["WIDTH"]!!.max.toInt()
                            val minHeight = message["HEIGHT"]!!.min.toInt()
                            val maxHeight = message["HEIGHT"]!!.max.toInt()

                            this.minX = minX
                            this.maxX = maxX
                            this.minY = minY
                            this.maxY = maxY
                            this.minWidth = minWidth
                            this.maxWidth = maxWidth
                            this.minHeight = minHeight
                            this.maxHeight = maxHeight
                        }

                        val x = message["X"]!!.value.toInt()
                        val y = message["Y"]!!.value.toInt()
                        val width = message["WIDTH"]!!.value.toInt()
                        val height = message["HEIGHT"]!!.value.toInt()

                        this.x = x
                        this.y = y
                        this.width = width
                        this.height = height

                        handler.fireOnEventReceived(CameraFrameChanged(this))
                    }
                    "CCD_BINNING" -> {
                        if (message is DefNumberVector) {
                            canBin = message.isNotReadOnly
                            maxBinX = message["HOR_BIN"]!!.max.toInt()
                            maxBinY = message["VER_BIN"]!!.max.toInt()

                            handler.fireOnEventReceived(CameraCanBinChanged(this))
                        }

                        binX = message["HOR_BIN"]!!.value.toInt()
                        binY = message["VER_BIN"]!!.value.toInt()

                        handler.fireOnEventReceived(CameraBinChanged(this))
                    }
                    "TELESCOPE_TIMED_GUIDE_NS",
                    "TELESCOPE_TIMED_GUIDE_WE" -> {
                        if (!canPulseGuide && message is DefNumberVector) {
                            canPulseGuide = true

                            handler.registerGuideOutput(this)

                            LOG.info("guide output attached: {}", name)
                        } else {
                            val prevIsPulseGuiding = pulseGuiding
                            pulseGuiding = message.isBusy

                            if (pulseGuiding != prevIsPulseGuiding) {
                                handler.fireOnEventReceived(GuideOutputPulsingChanged(this))
                            }
                        }
                    }
                }
            }
            is SetBLOBVector -> {
                when (message.name) {
                    "CCD1" -> {
                        val ccd1 = message["CCD1"]!!
                        val fits = Base64InputStream(ccd1.value)
                        val compressed = COMPRESSION_FORMATS.any { ccd1.format.endsWith(it, true) }
                        handler.fireOnEventReceived(CameraFrameCaptured(this, fits, compressed))
                    }
                    "CCD2" -> {
                        // TODO: Handle Guider Head frame.
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
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
        sendNewSwitch("CCD_FRAME_TYPE", "FRAME_$type" to true)
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

    override fun startCapture(exposureTime: Duration) {
        val exposureInSeconds = exposureTime.toNanos() / 1000000000.0
        sendNewNumber("CCD_EXPOSURE", "CCD_EXPOSURE_VALUE" to exposureInSeconds)
    }

    override fun abortCapture() {
        if (canAbort) {
            sendNewSwitch("CCD_ABORT_EXPOSURE", "ABORT" to true)
        }
    }

    override fun guideNorth(duration: Duration) {
        if (canPulseGuide) {
            sendNewNumber("TELESCOPE_TIMED_GUIDE_NS", "TIMED_GUIDE_N" to duration.toNanos() / 1000.0, "TIMED_GUIDE_S" to 0.0)
        }
    }

    override fun guideSouth(duration: Duration) {
        if (canPulseGuide) {
            sendNewNumber("TELESCOPE_TIMED_GUIDE_NS", "TIMED_GUIDE_S" to duration.toNanos() / 1000.0, "TIMED_GUIDE_N" to 0.0)
        }
    }

    override fun guideEast(duration: Duration) {
        if (canPulseGuide) {
            sendNewNumber("TELESCOPE_TIMED_GUIDE_WE", "TIMED_GUIDE_E" to duration.toNanos() / 1000.0, "TIMED_GUIDE_W" to 0.0)
        }
    }

    override fun guideWest(duration: Duration) {
        if (canPulseGuide) {
            sendNewNumber("TELESCOPE_TIMED_GUIDE_WE", "TIMED_GUIDE_W" to duration.toNanos() / 1000.0, "TIMED_GUIDE_E" to 0.0)
        }
    }

    override fun close() {
        if (hasThermometer) {
            handler.unregisterThermometer(this)
            hasThermometer = false
            LOG.info("thermometer detached: {}", name)
        }

        if (canPulseGuide) {
            handler.unregisterGuideOutput(this)
            canPulseGuide = false
            LOG.info("guide output detached: {}", name)
        }
    }

    protected fun processGain(message: NumberVector<*>, element: NumberElement) {
        if (message is DefNumberVector) {
            gainMin = element.min.toInt()
            gainMax = element.max.toInt()

            handler.fireOnEventReceived(CameraGainMinMaxChanged(this))
        }

        gain = element.value.toInt()

        handler.fireOnEventReceived(CameraGainChanged(this))
    }

    protected fun processOffset(message: NumberVector<*>, element: NumberElement) {
        if (message is DefNumberVector) {
            offsetMin = element.min.toInt()
            offsetMax = element.max.toInt()

            handler.fireOnEventReceived(CameraOffsetMinMaxChanged(this))
        }

        offset = element.value.toInt()

        handler.fireOnEventReceived(CameraOffsetChanged(this))
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

    companion object {

        @JvmStatic private val COMPRESSION_FORMATS = arrayOf(".fz", ".gz")
        @JvmStatic private val LOG = loggerFor<CameraDevice>()
    }
}
