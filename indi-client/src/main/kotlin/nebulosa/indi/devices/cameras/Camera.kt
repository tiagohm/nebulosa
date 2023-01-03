package nebulosa.indi.devices.cameras

import nebulosa.imaging.algorithms.CfaPattern
import nebulosa.indi.INDIClient
import nebulosa.indi.devices.Device
import nebulosa.indi.devices.DeviceProtocolHandler
import nebulosa.indi.devices.isOn
import nebulosa.indi.protocol.*
import nebulosa.io.Base64InputStream

class Camera(
    client: INDIClient,
    handler: DeviceProtocolHandler,
    name: String,
) : Device(client, handler, name) {

    @Volatile @JvmField var hasCoolerControl = false
    @Volatile @JvmField var isCoolerOn = false
    @Volatile @JvmField var hasDewHeater = false
    @Volatile @JvmField var isDewHeaterOn = false
    @Volatile @JvmField var frameFormats = emptyList<String>()
    @Volatile @JvmField var canAbort = false
    @Volatile @JvmField var cfaOffsetX = 0
    @Volatile @JvmField var cfaOffsetY = 0
    @Volatile @JvmField var cfaType = CfaPattern.RGGB
    @Volatile @JvmField var exposureMin = 0L
    @Volatile @JvmField var exposureMax = 0L
    @Volatile @JvmField var exposureState = PropertyState.IDLE
    @Volatile @JvmField var exposure = 0L
    @Volatile @JvmField var hasCooler = false
    @Volatile @JvmField var canSetTemperature = false
    @Volatile @JvmField var temperature = 0.0
    @Volatile @JvmField var canSubFrame = false
    @Volatile @JvmField var x = 0
    @Volatile @JvmField var minX = 0
    @Volatile @JvmField var maxX = 0
    @Volatile @JvmField var y = 0
    @Volatile @JvmField var minY = 0
    @Volatile @JvmField var maxY = 0
    @Volatile @JvmField var width = 0
    @Volatile @JvmField var minWidth = 0
    @Volatile @JvmField var maxWidth = 0
    @Volatile @JvmField var height = 0
    @Volatile @JvmField var minHeight = 0
    @Volatile @JvmField var maxHeight = 0
    @Volatile @JvmField var canBin = false
    @Volatile @JvmField var maxBinX = 1
    @Volatile @JvmField var maxBinY = 1
    @Volatile @JvmField var binX = 1
    @Volatile @JvmField var binY = 1
    @Volatile @JvmField var gain = 0
    @Volatile @JvmField var gainMin = 0
    @Volatile @JvmField var gainMax = 0
    @Volatile @JvmField var offset = 0
    @Volatile @JvmField var offsetMin = 0
    @Volatile @JvmField var offsetMax = 0

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
                        exposure = (element.value * 1000000.0).toLong()

                        handler.fireOnEventReceived(CameraExposureStateChanged(this, prevExposureState))
                    }
                    "CCD_TEMPERATURE" -> {
                        if (message is DefNumberVector) {
                            hasCooler = true
                            canSetTemperature = message.perm != PropertyPermission.RO

                            handler.fireOnEventReceived(CameraHasCoolerChanged(this))
                            handler.fireOnEventReceived(CameraCanSetTemperatureChanged(this))
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
                }
            }
            is SetBLOBVector -> {
                when (message.name) {
                    "CCD1" -> {
                        val ccd1 = message["CCD1"]!!
                        // TODO: Handle zipped format.
                        val fits = Base64InputStream(ccd1.value)
                        handler.fireOnEventReceived(CameraExposureFrame(this, fits))
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    @Synchronized
    fun ask() {
        client.sendMessageToServer(GetProperties().also { it.device = name })
    }

    @Synchronized
    fun cooler(enable: Boolean) {
        if (hasCoolerControl) {
            sendNewSwitch("CCD_COOLER", "COOLER_ON" to enable, "COOLER_OFF" to !enable)
        }
    }

    @Synchronized
    fun temperature(value: Double) {
        if (canSetTemperature) {
            sendNewNumber("CCD_TEMPERATURE", "CCD_TEMPERATURE_VALUE" to value)
        }
    }

    @Synchronized
    fun frameFormat(format: String) {
        sendNewSwitch("CCD_CAPTURE_FORMAT", format to true)
    }

    @Synchronized
    fun frameType(type: FrameType) {
        sendNewSwitch("CCD_FRAME_TYPE", "FRAME_$type" to true)
    }

    @Synchronized
    fun frame(x: Int, y: Int, width: Int, height: Int) {
        if (canSubFrame) {
            sendNewNumber(
                "CCD_FRAME", "X" to x.toDouble(), "Y" to y.toDouble(),
                "WIDTH" to width.toDouble(), "HEIGHT" to height.toDouble(),
            )
        }
    }

    @Synchronized
    fun bin(x: Int, y: Int) {
        sendNewNumber("CCD_BINNING", "HOR_BIN" to x.toDouble(), "VER_BIN" to y.toDouble())
    }

    @Synchronized
    fun gain(value: Int) {
        sendNewNumber("CCD_GAIN", "GAIN" to value.toDouble())
    }

    @Synchronized
    fun offset(value: Int) {
        sendNewNumber("CCD_OFFSET", "OFFSET" to value.toDouble())
    }

    @Synchronized
    fun startCapture(exposureInMicros: Long) {
        val exposureInSeconds = exposureInMicros / 1000000.0
        sendNewNumber("CCD_EXPOSURE", "CCD_EXPOSURE_VALUE" to exposureInSeconds)
    }

    @Synchronized
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
