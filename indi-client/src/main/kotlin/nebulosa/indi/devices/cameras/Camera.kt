package nebulosa.indi.devices.cameras

import nebulosa.fits.CfaPattern
import nebulosa.indi.INDIClient
import nebulosa.indi.devices.Device
import nebulosa.indi.devices.DeviceProtocolHandler
import nebulosa.indi.devices.events.*
import nebulosa.indi.devices.isOn
import nebulosa.indi.protocol.*
import nebulosa.io.Base64InputStream

class Camera(
    client: INDIClient,
    handler: DeviceProtocolHandler,
    name: String,
) : Device(client, handler, name) {

    @Volatile var hasCoolerControl = false
        private set

    @Volatile var isCoolerOn = false
        private set

    @Volatile var frameFormats = emptyList<FrameFormat>()
        private set

    @Volatile var canAbort = false
        private set

    @Volatile var cfaOffsetX = 0
        private set

    @Volatile var cfaOffsetY = 0
        private set

    @Volatile var cfaType = CfaPattern.RGGB
        private set

    @Volatile var exposureMin = 0L
        private set

    @Volatile var exposureMax = 0L
        private set

    @Volatile var exposureState = PropertyState.IDLE
        private set

    @Volatile var hasCooler = false
        private set

    @Volatile var canSetTemperature = false
        private set

    @Volatile var temperature = 0.0
        private set

    @Volatile var canSubframe = false
        private set

    @Volatile var x = 0
        private set

    @Volatile var minX = 0
        private set

    @Volatile var maxX = 0
        private set

    @Volatile var y = 0
        private set

    @Volatile var minY = 0
        private set

    @Volatile var maxY = 0
        private set

    @Volatile var width = 0
        private set

    @Volatile var minWidth = 0
        private set

    @Volatile var maxWidth = 0
        private set

    @Volatile var height = 0
        private set

    @Volatile var minHeight = 0
        private set

    @Volatile var maxHeight = 0
        private set

    @Volatile var canBin = false
        private set

    @Volatile var maxBinX = 1
        private set

    @Volatile var maxBinY = 1
        private set

    @Volatile var binX = 1
        private set

    @Volatile var binY = 1
        private set

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "CCD_COOLER" -> {
                        hasCoolerControl = true
                        isCoolerOn = message["COOLER_ON"]!!.isOn()
                    }
                    "CCD_CAPTURE_FORMAT" -> {
                        if (message is DefSwitchVector) {
                            frameFormats = message.map { FrameFormat(it.name, it.label) }
                        }
                    }
                    "CCD_ABORT_EXPOSURE" -> {
                        if (message is DefSwitchVector) {
                            canAbort = message.perm != PropertyPermission.RO
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
                        }

                        val prevExposureState = exposureState
                        exposureState = message.state

                        when (exposureState) {
                            PropertyState.BUSY -> {
                                val exposure = (element.value * 1000000.0).toLong()
                                handler.fireOnEventReceived(CameraExposureBusyEvent(this, exposure))
                            }
                            PropertyState.ALERT -> {
                                handler.fireOnEventReceived(CameraExposureFailedEvent(this))
                            }
                            PropertyState.OK -> {
                                handler.fireOnEventReceived(CameraExposureOkEvent(this))
                            }
                            PropertyState.IDLE -> {
                                if (prevExposureState != PropertyState.IDLE) {
                                    handler.fireOnEventReceived(CameraExposureAbortedEvent(this))
                                }
                            }
                        }
                    }
                    "CCD_TEMPERATURE" -> {
                        if (message is DefNumberVector) {
                            hasCooler = true
                            canSetTemperature = message.perm != PropertyPermission.RO
                        }

                        temperature = message["CCD_TEMPERATURE_VALUE"]!!.value
                    }
                    "CCD_FRAME" -> {
                        if (message is DefNumberVector) {
                            canSubframe = message.perm != PropertyPermission.RO
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
                    }
                    "CCD_BINNING" -> {
                        if (message is DefNumberVector) {
                            canBin = message.perm != PropertyPermission.RO
                            maxBinX = message["HOR_BIN"]!!.max.toInt()
                            maxBinY = message["VER_BIN"]!!.max.toInt()
                        }

                        binX = message["HOR_BIN"]!!.value.toInt()
                        binY = message["VER_BIN"]!!.value.toInt()
                    }
                }
            }
            is SetBLOBVector -> {
                when (message.name) {
                    "CCD1" -> {
                        val ccd1 = message["CCD1"]!!
                        // TODO: Handle zipped format.
                        val fits = Base64InputStream(ccd1.value)
                        handler.fireOnEventReceived(CameraExposureFrameEvent(this, fits))
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    fun ask() {
        client.sendMessageToServer(GetProperties().also { it.device = name })
    }

    fun cooler(enable: Boolean) {
        if (hasCoolerControl) {
            sendNewSwitch("CCD_COOLER", "COOLER_ON" to enable, "COOLER_OFF" to !enable)
        }
    }

    fun temperature(value: Double) {
        if (canSetTemperature) {
            sendNewNumber("CCD_TEMPERATURE", "CCD_TEMPERATURE_VALUE" to value)
        }
    }

    fun frameFormat(format: FrameFormat) {
        sendNewSwitch("CCD_CAPTURE_FORMAT", format.name to true)
    }

    fun frameType(type: FrameType) {
        sendNewSwitch("CCD_FRAME_TYPE", "FRAME_$type" to true)
    }

    fun frame(x: Int, y: Int, width: Int, height: Int) {
        if (canSubframe) {
            sendNewNumber(
                "CCD_FRAME", "X" to x.toDouble(), "Y" to y.toDouble(),
                "WIDTH" to width.toDouble(), "HEIGHT" to height.toDouble(),
            )
        }
    }

    fun bin(x: Int, y: Int) {
        sendNewNumber("CCD_BINNING", "HOR_BIN" to x.toDouble(), "VER_BIN" to y.toDouble())
    }

    fun startCapture(exposureInMicros: Long) {
        val exposureInSeconds = exposureInMicros / 1000000.0
        sendNewNumber("CCD_EXPOSURE", "CCD_EXPOSURE_VALUE" to exposureInSeconds)
    }

    fun abortCapture() {
        if (canAbort) {
            sendNewSwitch("CCD_ABORT_EXPOSURE", "ABORT" to true)
        }
    }

    override fun toString() = name

    companion object {

        @JvmStatic val DRIVERS = setOf(
            "indi_altair_ccd",
            "indi_apogee_ccd",
            "indi_asi_ccd",
            "indi_asi_single_ccd",
            "indi_atik_ccd",
            "indi_cam90_ccd",
            "indi_canon_ccd",
            "indi_dsi_ccd",
            "indi_ffmv_ccd",
            "indi_fishcamp_ccd",
            "indi_fli_ccd",
            "indi_fuji_ccd",
            "indi_gphoto_ccd",
            "indi_inovaplx_ccd",
            "indi_mallincam_ccd",
            "indi_mi_ccd_eth",
            "indi_mi_ccd_usb",
            "indi_nightscape_ccd",
            "indi_nikon_ccd",
            "indi_nncam_ccd",
            "indi_omegonprocam_ccd",
            "indi_orion_ssg3_ccd",
            "indi_pentax_ccd",
            "indi_pentax",
            "indi_playerone_ccd",
            "indi_qhy_ccd",
            "indi_qsi_ccd",
            "indi_rpicam",
            "indi_sbig_ccd",
            "indi_simulator_ccd",
            "indi_simulator_guide",
            "indi_sony_ccd",
            "indi_starshootg_ccd",
            "indi_svbony_ccd",
            "indi_sx_ccd",
            "indi_toupcam_ccd",
            "indi_v4l2_ccd",
            "indi_webcam_ccd",
            "indi_kepler_ccd",
        )
    }
}
