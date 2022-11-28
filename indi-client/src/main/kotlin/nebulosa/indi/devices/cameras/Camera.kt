package nebulosa.indi.devices.cameras

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

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "CCD_COOLER" -> {
                        handler.fireOnEventReceived(this, CameraHasCoolerControlEvent(this))
                        val enabled = message["COOLER_ON"]!!.isOn()
                        handler.fireOnEventReceived(this, CameraCoolerToggledEvent(this, enabled))
                    }
                    "CCD_CAPTURE_FORMAT" -> {
                        if (message is DefSwitchVector) {
                            val formats = message.map { CaptureFormat(it.name, it.label) }
                            handler.fireOnEventReceived(this, CameraCaptureFormatEvent(this, formats))
                        }
                    }
                    "CCD_ABORT_EXPOSURE" -> {
                        if (message is DefSwitchVector) {
                            val enabled = message.perm != PropertyPermission.RO
                            handler.fireOnEventReceived(this, CameraCanAbortEvent(this, enabled))
                        }
                    }
                }
            }
            is TextVector<*> -> {
                when (message.name) {
                    "CCD_CFA" -> {
                        val offsetX = message["CFA_OFFSET_X"]!!.value.toInt()
                        val offsetY = message["CFA_OFFSET_Y"]!!.value.toInt()
                        val type = CfaPattern.valueOf(message["CFA_TYPE"]!!.value)
                        handler.fireOnEventReceived(this, CameraCfaEvent(this, offsetX, offsetY, type))
                    }
                }
            }
            is NumberVector<*> -> {
                when (message.name) {
                    "CCD_EXPOSURE" -> {
                        val element = message["CCD_EXPOSURE_VALUE"]!!

                        if (element is DefNumber) {
                            val min = (element.min * 1000000.0).toLong()
                            val max = (element.max * 1000000.0).toLong()
                            handler.fireOnEventReceived(this, CameraExposureMinMaxEvent(this, min, max))
                        }

                        when (message.state) {
                            PropertyState.BUSY -> {
                                val exposure = (element.value * 1000000.0).toLong()
                                handler.fireOnEventReceived(this, CameraExposureBusyEvent(this, exposure))
                            }
                            PropertyState.ALERT -> {
                                handler.fireOnEventReceived(this, CameraExposureFailedEvent(this))
                            }
                            PropertyState.OK -> {
                                handler.fireOnEventReceived(this, CameraExposureOkEvent(this))
                            }
                            PropertyState.IDLE -> {
                                handler.fireOnEventReceived(this, CameraExposureAbortedEvent(this))
                            }
                        }
                    }
                    "CCD_TEMPERATURE" -> {
                        if (message is DefNumberVector) {
                            handler.fireOnEventReceived(this, CameraHasCoolerEvent(this))
                            val enabled = message.perm != PropertyPermission.RO
                            handler.fireOnEventReceived(this, CameraCanSetTemperatureEvent(this, enabled))
                        }

                        val temperature = message["CCD_TEMPERATURE_VALUE"]!!.value
                        handler.fireOnEventReceived(this, CameraTemperatureChangedEvent(this, temperature))
                    }
                    "CCD_FRAME" -> {
                        if (message is DefNumberVector) {
                            val enabled = message.perm != PropertyPermission.RO
                            handler.fireOnEventReceived(this, CameraCanSubframeEvent(this, enabled))
                        }

                        val minX = message["X"]!!.min.toInt()
                        val maxX = message["X"]!!.max.toInt()
                        val minY = message["Y"]!!.min.toInt()
                        val maxY = message["Y"]!!.max.toInt()
                        val minWidth = message["WIDTH"]!!.min.toInt()
                        val maxWidth = message["WIDTH"]!!.max.toInt()
                        val minHeight = message["HEIGHT"]!!.min.toInt()
                        val maxHeight = message["HEIGHT"]!!.max.toInt()

                        if (maxX != 0 && maxY != 0 &&
                            maxWidth != 0 && maxHeight != 0
                        ) {
                            handler.fireOnEventReceived(
                                this,
                                CameraSubframeMinMaxEvent(
                                    this, minX, maxX, minY, maxY, minWidth, maxWidth, minHeight, maxHeight
                                )
                            )
                        }

                        val x = message["X"]!!.value.toInt()
                        val y = message["Y"]!!.value.toInt()
                        val width = message["WIDTH"]!!.value.toInt()
                        val height = message["HEIGHT"]!!.value.toInt()

                        handler.fireOnEventReceived(this, CameraSubframeChangedEvent(this, x, y, width, height))
                    }
                    "CCD_BINNING" -> {
                        if (message is DefNumberVector) {
                            val enabled = message.perm != PropertyPermission.RO
                            handler.fireOnEventReceived(this, CameraCanBinEvent(this, enabled))

                            val maxX = message["HOR_BIN"]!!.max.toInt()
                            val maxY = message["VER_BIN"]!!.max.toInt()
                            handler.fireOnEventReceived(this, CameraBinMinMaxEvent(this, maxX, maxY))
                        }

                        val x = message["HOR_BIN"]!!.value.toInt()
                        val y = message["VER_BIN"]!!.value.toInt()
                        handler.fireOnEventReceived(this, CameraBinChangedEvent(this, x, y))
                    }
                }
            }
            is SetBLOBVector -> {
                when (message.name) {
                    "CCD1" -> {
                        val ccd1 = message["CCD1"]!!
                        // TODO: Handle zipped format.
                        val fits = Base64InputStream(ccd1.value)
                        handler.fireOnEventReceived(this, CameraExposureFrameEvent(this, fits))
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
        sendNewSwitch("CCD_COOLER", "COOLER_ON" to enable, "COOLER_OFF" to !enable)
    }

    fun temperature(value: Double) {
        sendNewNumber("CCD_TEMPERATURE", "CCD_TEMPERATURE_VALUE" to value)
    }

    fun frameFormat(format: CaptureFormat) {
        sendNewSwitch("CCD_CAPTURE_FORMAT", format.name to true)
    }

    fun frameType(type: FrameType) {
        sendNewSwitch("CCD_FRAME_TYPE", "FRAME_$type" to true)
    }

    fun frame(x: Int, y: Int, width: Int, height: Int) {
        sendNewNumber(
            "CCD_FRAME", "X" to x.toDouble(), "Y" to y.toDouble(),
            "WIDTH" to width.toDouble(), "HEIGHT" to height.toDouble(),
        )
    }

    fun bin(x: Int, y: Int) {
        sendNewNumber("CCD_BINNING", "HOR_BIN" to x.toDouble(), "VER_BIN" to y.toDouble())
    }

    fun startCapture(exposureInMicros: Long) {
        val exposureInSeconds = exposureInMicros / 1000000.0
        sendNewNumber("CCD_EXPOSURE", "CCD_EXPOSURE_VALUE" to exposureInSeconds)
    }

    fun abortCapture() {
        sendNewSwitch("CCD_ABORT_EXPOSURE", "ABORT" to true)
    }

    override fun toString() = name

    companion object {

        @JvmStatic
        val DRIVERS = setOf(
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
            "indi_sv305_ccd",
            "indi_sx_ccd",
            "indi_toupcam_ccd",
            "indi_v4l2_ccd",
            "indi_webcam_ccd",
        )
    }
}
