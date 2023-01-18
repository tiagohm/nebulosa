package nebulosa.indi.device.cameras

import nebulosa.imaging.algorithms.CfaPattern
import nebulosa.indi.device.guiders.Guider
import nebulosa.indi.device.thermometers.Thermometer
import nebulosa.indi.protocol.PropertyState

interface Camera : Guider, Thermometer {

    val isCapturing: Boolean

    val hasCoolerControl: Boolean

    val isCoolerOn: Boolean

    val hasDewHeater: Boolean

    val isDewHeaterOn: Boolean

    val frameFormats: List<String>

    val canAbort: Boolean

    val cfaOffsetX: Int

    val cfaOffsetY: Int

    val cfaType: CfaPattern

    val exposureMin: Long

    val exposureMax: Long

    val exposureState: PropertyState

    val exposure: Long

    val hasCooler: Boolean

    val canSetTemperature: Boolean

    val canSubFrame: Boolean

    val x: Int

    val minX: Int

    val maxX: Int

    val y: Int

    val minY: Int

    val maxY: Int

    val width: Int

    val minWidth: Int

    val maxWidth: Int

    val height: Int

    val minHeight: Int

    val maxHeight: Int

    val canBin: Boolean

    val maxBinX: Int

    val maxBinY: Int

    val binX: Int

    val binY: Int

    val gain: Int

    val gainMin: Int

    val gainMax: Int

    val offset: Int

    val offsetMin: Int

    val offsetMax: Int

    val hasGuiderHead: Boolean

    fun cooler(enable: Boolean)

    fun temperature(value: Double)

    fun frameFormat(format: String)

    fun frameType(type: FrameType)

    fun frame(x: Int, y: Int, width: Int, height: Int)

    fun bin(x: Int, y: Int)

    fun gain(value: Int)

    fun offset(value: Int)

    fun startCapture(exposureInMicros: Long)

    fun abortCapture()

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
