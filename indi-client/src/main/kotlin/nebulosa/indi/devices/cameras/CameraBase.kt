package nebulosa.indi.devices.cameras

import nebulosa.imaging.algorithms.CfaPattern
import nebulosa.indi.INDIClient
import nebulosa.indi.devices.AbstractDevice
import nebulosa.indi.devices.DeviceProtocolHandler
import nebulosa.indi.devices.guiders.GuiderAttached
import nebulosa.indi.devices.guiders.GuiderDetached
import nebulosa.indi.devices.guiders.GuiderPulsingChanged
import nebulosa.indi.devices.isOn
import nebulosa.indi.devices.thermometers.ThermometerAttached
import nebulosa.indi.devices.thermometers.ThermometerDetached
import nebulosa.indi.protocol.*
import nebulosa.io.Base64InputStream

internal open class CameraBase(
    client: INDIClient,
    handler: DeviceProtocolHandler,
    name: String,
) : AbstractDevice(client, handler, name), Camera {

    override var isCapturing = false
    override var hasCoolerControl = false
    override var isCoolerOn = false
    override var hasDewHeater = false
    override var isDewHeaterOn = false
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

    override var hasThermometer = false
    override var temperature = 0.0

    override var canPulseGuide = false
    override var isPulseGuiding = false

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "CCD_COOLER" -> {
                        hasCoolerControl = true
                        isCoolerOn = message["COOLER_ON"]!!.isOn()

                        handler.fireOnEventReceived(CameraCoolerControlChanged(this))
                        handler.fireOnEventReceived(CameraCoolerChanged(this))
                    }
                    "CCD_CAPTURE_FORMAT" -> {
                        if (message is DefSwitchVector) {
                            frameFormats = message.map { it.name }
                            handler.fireOnEventReceived(CameraFrameFormatsChanged(this))
                        }
                    }
                    "CCD_ABORT_EXPOSURE" -> {
                        if (message is DefSwitchVector) {
                            canAbort = message.perm != PropertyPermission.RO
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

                        val prevIsCapturing = isCapturing
                        isCapturing = exposureState == PropertyState.BUSY

                        if (prevIsCapturing != isCapturing) {
                            handler.fireOnEventReceived(CameraCapturingChanged(this))
                        }

                        if (exposureState == PropertyState.IDLE
                            && (prevExposureState == PropertyState.BUSY || isCapturing)
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
                    "CCD_TEMPERATURE" -> {
                        if (message is DefNumberVector) {
                            hasCooler = true
                            canSetTemperature = message.perm != PropertyPermission.RO

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
                            canSubFrame = message.perm != PropertyPermission.RO
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

                        if (maxX != 0 && maxY != 0 && maxWidth != 0 && maxHeight != 0) {
                            this.minX = minX
                            this.maxX = maxX
                            this.minY = minY
                            this.maxY = maxY
                            this.minWidth = minWidth
                            this.maxWidth = maxWidth
                            this.minHeight = minHeight
                            this.maxHeight = maxHeight
                        }

                        x = message["X"]!!.value.toInt()
                        y = message["Y"]!!.value.toInt()
                        width = message["WIDTH"]!!.value.toInt()
                        height = message["HEIGHT"]!!.value.toInt()

                        handler.fireOnEventReceived(CameraFrameChanged(this))
                    }
                    "CCD_BINNING" -> {
                        if (message is DefNumberVector) {
                            canBin = message.perm != PropertyPermission.RO
                            maxBinX = message["HOR_BIN"]!!.max.toInt()
                            maxBinY = message["VER_BIN"]!!.max.toInt()

                            handler.fireOnEventReceived(CameraCanBinChanged(this))
                        }

                        binX = message["HOR_BIN"]!!.value.toInt()
                        binY = message["VER_BIN"]!!.value.toInt()

                        handler.fireOnEventReceived(CameraBinChanged(this))
                    }
                    "CCD_GAIN" -> {
                        if (message is DefNumberVector) {
                            gainMin = message["GAIN"]!!.min.toInt()
                            gainMax = message["GAIN"]!!.max.toInt()

                            handler.fireOnEventReceived(CameraGainMinMaxChanged(this))
                        }

                        gain = message["GAIN"]!!.value.toInt()

                        handler.fireOnEventReceived(CameraGainChanged(this))
                    }
                    "CCD_OFFSET" -> {
                        if (message is DefNumberVector) {
                            offsetMin = message["OFFSET"]!!.min.toInt()
                            offsetMax = message["OFFSET"]!!.max.toInt()

                            handler.fireOnEventReceived(CameraOffsetMinMaxChanged(this))
                        }

                        offset = message["OFFSET"]!!.value.toInt()

                        handler.fireOnEventReceived(CameraOffsetChanged(this))
                    }
                    "TELESCOPE_TIMED_GUIDE_NS" -> {
                        if (!canPulseGuide && message is DefNumberVector) {
                            canPulseGuide = true

                            handler.fireOnEventReceived(GuiderAttached(this))
                        } else {
                            val prevIsPulseGuiding = isPulseGuiding
                            isPulseGuiding = message.state == PropertyState.BUSY

                            if (isPulseGuiding != prevIsPulseGuiding) {
                                handler.fireOnEventReceived(GuiderPulsingChanged(this))
                            }
                        }
                    }
                    "TELESCOPE_TIMED_GUIDE_WE" -> {
                        if (!canPulseGuide && message is DefNumberVector) {
                            canPulseGuide = true

                            handler.fireOnEventReceived(GuiderAttached(this))
                        } else {
                            val prevIsPulseGuiding = isPulseGuiding
                            isPulseGuiding = message.state == PropertyState.BUSY

                            if (isPulseGuiding != prevIsPulseGuiding) {
                                handler.fireOnEventReceived(GuiderPulsingChanged(this))
                            }
                        }
                    }
                }
            }
            is SetBLOBVector -> {
                when (message.name) {
                    "CCD1" -> {
                        val ccd1 = message["CCD1"]!!
                        // TODO: Handle zipped format.
                        val fits = Base64InputStream(ccd1.value)
                        handler.fireOnEventReceived(CameraFrameCaptured(this, fits))
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
        if (hasCoolerControl) {
            sendNewSwitch("CCD_COOLER", "COOLER_ON" to enable, "COOLER_OFF" to !enable)
        }
    }

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

    override fun gain(value: Int) {
        sendNewNumber("CCD_GAIN", "GAIN" to value.toDouble())
    }

    override fun offset(value: Int) {
        sendNewNumber("CCD_OFFSET", "OFFSET" to value.toDouble())
    }

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
            handler.fireOnEventReceived(GuiderDetached(this))
        }
    }

    override fun toString(): String {
        return "Camera(name=$name, isCapturing=$isCapturing," +
                " hasCoolerControl=$hasCoolerControl, isCoolerOn=$isCoolerOn," +
                " hasDewHeater=$hasDewHeater, isDewHeaterOn=$isDewHeaterOn," +
                " frameFormats=$frameFormats, canAbort=$canAbort," +
                " cfaOffsetX=$cfaOffsetX, cfaOffsetY=$cfaOffsetY, cfaType=$cfaType," +
                " exposureMin=$exposureMin, exposureMax=$exposureMax," +
                " exposureState=$exposureState, exposure=$exposure," +
                " hasCooler=$hasCooler, canSetTemperature=$canSetTemperature," +
                " temperature=$temperature, canSubFrame=$canSubFrame," +
                " x=$x, minX=$minX, maxX=$maxX, y=$y, minY=$minY, maxY=$maxY," +
                " width=$width, minWidth=$minWidth, maxWidth=$maxWidth, height=$height," +
                " minHeight=$minHeight, maxHeight=$maxHeight," +
                " canBin=$canBin, maxBinX=$maxBinX, maxBinY=$maxBinY," +
                " binX=$binX, binY=$binY, gain=$gain, gainMin=$gainMin," +
                " gainMax=$gainMax, offset=$offset, offsetMin=$offsetMin," +
                " offsetMax=$offsetMax, hasGuiderHead=$hasGuiderHead," +
                " canPulseGuide=$canPulseGuide, isPulseGuiding=$isPulseGuiding)"
    }
}
