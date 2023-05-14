package nebulosa.indi.client.device.camera

import nebulosa.imaging.algorithms.CfaPattern
import nebulosa.indi.client.device.AbstractDevice
import nebulosa.indi.client.device.DeviceProtocolHandler
import nebulosa.indi.device.MessageSender
import nebulosa.indi.device.camera.*
import nebulosa.indi.device.guide.GuideOutputAttached
import nebulosa.indi.device.guide.GuideOutputDetached
import nebulosa.indi.device.guide.GuideOutputPulsingChanged
import nebulosa.indi.device.thermometer.ThermometerAttached
import nebulosa.indi.device.thermometer.ThermometerDetached
import nebulosa.indi.protocol.*
import nebulosa.io.Base64InputStream

internal open class CameraDevice(
    sender: MessageSender,
    handler: DeviceProtocolHandler,
    name: String,
) : AbstractDevice(sender, handler, name), Camera {

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
    override var exposureMin = 0L
    override var exposureMax = 0L
    override var exposureState = PropertyState.IDLE
    override var exposure = 0L
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
    override var hasGuiderHead = false // TODO: Handle guider head.
    override var pixelSizeX = 0.0
    override var pixelSizeY = 0.0

    override var hasThermometer = false
    override var temperature = 0.0

    override var canPulseGuide = false
    override var pulseGuiding = false

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
                            exposureMin = (element.min * 1000000.0).toLong()
                            exposureMax = (element.max * 1000000.0).toLong()
                            handler.fireOnEventReceived(CameraExposureMinMaxChanged(this))
                        }

                        val prevExposureState = exposureState
                        exposureState = message.state

                        if (exposureState == PropertyState.BUSY || exposureState == PropertyState.OK) {
                            exposure = (element.value * 1000000.0).toLong()

                            handler.fireOnEventReceived(CameraExposureProgressChanged(this))
                        }

                        val prevIsExposuring = exposuring
                        exposuring = exposureState == PropertyState.BUSY

                        if (prevIsExposuring != exposuring) {
                            handler.fireOnEventReceived(CameraExposuringChanged(this))
                        }

                        if (exposureState == PropertyState.IDLE
                            && (prevExposureState == PropertyState.BUSY || exposuring)
                        ) {
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
                                handler.fireOnEventReceived(ThermometerAttached(this))
                            }
                        }

                        temperature = message["CCD_TEMPERATURE_VALUE"]!!.value
                        handler.fireOnEventReceived(CameraTemperatureChanged(this))
                    }
                    "CCD_FRAME" -> {
                        if (message is DefNumberVector) {
                            canSubFrame = message.isNotReadOnly
                            handler.fireOnEventReceived(CameraCanSubFrameChanged(this))
                        }

                        val minX = message["X"]!!.min.toInt()
                        val maxX = message["X"]!!.max.toInt()
                        val minY = message["Y"]!!.min.toInt()
                        val maxY = message["Y"]!!.max.toInt()
                        val minWidth = message["WIDTH"]!!.min.toInt()
                        val maxWidth = message["WIDTH"]!!.max.toInt()
                        val minHeight = message["HEIGHT"]!!.min.toInt()
                        val maxHeight = message["HEIGHT"]!!.max.toInt()
                        val x = message["X"]!!.value.toInt()
                        val y = message["Y"]!!.value.toInt()
                        val width = message["WIDTH"]!!.value.toInt()
                        val height = message["HEIGHT"]!!.value.toInt()

                        val changed = maxX != 0 && maxY != 0 &&
                                maxWidth != 0 && maxHeight != 0 &&
                                minWidth != 0 && minHeight != 0 &&
                                (minX != this.minX ||
                                        maxX != this.maxX ||
                                        minY != this.minY ||
                                        maxY != this.maxY ||
                                        minWidth != this.minWidth ||
                                        maxWidth != this.maxWidth ||
                                        minHeight != this.minHeight ||
                                        maxHeight != this.maxHeight ||
                                        x != this.x ||
                                        y != this.y ||
                                        width != this.width ||
                                        height != this.height)

                        if (changed) {
                            this.minX = minX
                            this.maxX = maxX
                            this.minY = minY
                            this.maxY = maxY
                            this.minWidth = minWidth
                            this.maxWidth = maxWidth
                            this.minHeight = minHeight
                            this.maxHeight = maxHeight

                            this.x = x
                            this.y = y
                            this.width = width
                            this.height = height

                            handler.fireOnEventReceived(CameraFrameChanged(this))
                        }
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

                            handler.fireOnEventReceived(GuideOutputAttached(this))
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

    override fun cooler(enable: Boolean) {
        if (hasCoolerControl && cooler != enable) {
            sendNewSwitch("CCD_COOLER", (if (enable) "COOLER_ON" else "COOLER_OFF") to true)
        }
    }

    override fun dewHeater(enable: Boolean) = Unit

    override fun temperature(value: Double) {
        if (canSetTemperature) {
            sendNewNumber("CCD_TEMPERATURE", "CCD_TEMPERATURE_VALUE" to value)
        }
    }

    override fun frameFormat(format: String) {
        sendNewSwitch("CCD_CAPTURE_FORMAT", format to true)
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

    override fun startCapture(exposureInMicros: Long) {
        val exposureInSeconds = exposureInMicros / 1000000.0
        sendNewNumber("CCD_EXPOSURE", "CCD_EXPOSURE_VALUE" to exposureInSeconds)
    }

    override fun abortCapture() {
        if (canAbort) {
            sendNewSwitch("CCD_ABORT_EXPOSURE", "ABORT" to true)
        }
    }

    override fun guideNorth(duration: Int) {
        if (canPulseGuide) {
            sendNewNumber("TELESCOPE_TIMED_GUIDE_NS", "TIMED_GUIDE_N" to duration.toDouble())
        }
    }

    override fun guideSouth(duration: Int) {
        if (canPulseGuide) {
            sendNewNumber("TELESCOPE_TIMED_GUIDE_NS", "TIMED_GUIDE_S" to duration.toDouble())
        }
    }

    override fun guideEast(duration: Int) {
        if (canPulseGuide) {
            sendNewNumber("TELESCOPE_TIMED_GUIDE_WE", "TIMED_GUIDE_E" to duration.toDouble())
        }
    }

    override fun guideWest(duration: Int) {
        if (canPulseGuide) {
            sendNewNumber("TELESCOPE_TIMED_GUIDE_WE", "TIMED_GUIDE_W" to duration.toDouble())
        }
    }

    override fun close() {
        if (hasThermometer) {
            hasThermometer = false
            handler.fireOnEventReceived(ThermometerDetached(this))
        }

        if (canPulseGuide) {
            canPulseGuide = false
            handler.fireOnEventReceived(GuideOutputDetached(this))
        }
    }

    override fun toString(): String {
        return "Camera(name=$name, exposuring=$exposuring," +
                " hasCoolerControl=$hasCoolerControl, cooler=$cooler," +
                " hasDewHeater=$hasDewHeater, dewHeater=$dewHeater," +
                " frameFormats=$frameFormats, canAbort=$canAbort," +
                " cfaOffsetX=$cfaOffsetX, cfaOffsetY=$cfaOffsetY, cfaType=$cfaType," +
                " exposureMin=$exposureMin, exposureMax=$exposureMax," +
                " exposureState=$exposureState, exposure=$exposure," +
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

    companion object {

        @JvmStatic private val COMPRESSION_FORMATS = arrayOf(".fz", ".gz")
    }
}
