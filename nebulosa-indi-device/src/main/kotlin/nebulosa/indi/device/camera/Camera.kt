package nebulosa.indi.device.camera

import nebulosa.image.algorithms.transformation.CfaPattern
import nebulosa.image.format.HeaderCard
import nebulosa.indi.device.DeviceType
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.indi.protocol.PropertyState
import java.time.Duration

interface Camera : GuideOutput, Thermometer {

    override val type
        get() = DeviceType.CAMERA

    val exposuring: Boolean

    val hasCoolerControl: Boolean

    val coolerPower: Double

    val cooler: Boolean

    val hasDewHeater: Boolean

    val dewHeater: Boolean

    val frameFormats: List<String>

    val canAbort: Boolean

    val cfaOffsetX: Int

    val cfaOffsetY: Int

    val cfaType: CfaPattern

    val exposureMin: Duration

    val exposureMax: Duration

    val exposureState: PropertyState

    val exposureTime: Duration

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

    val guideHead: GuideHead?

    val pixelSizeX: Double

    val pixelSizeY: Double

    fun cooler(enabled: Boolean)

    fun dewHeater(enabled: Boolean)

    fun temperature(value: Double)

    fun frameFormat(format: String?)

    fun frameType(type: FrameType)

    fun frame(x: Int, y: Int, width: Int, height: Int)

    fun bin(x: Int, y: Int)

    fun gain(value: Int)

    fun offset(value: Int)

    fun startCapture(exposureTime: Duration)

    fun abortCapture()

    fun fitsKeywords(vararg cards: HeaderCard)

    companion object {

        const val NANO_TO_SECONDS = 1_000_000_000.0

        @JvmStatic val DRIVERS = setOf(
            "indi_altair_ccd",
            "indi_apogee_ccd",
            "indi_asi_ccd",
            "indi_atik_ccd",
            "indi_bressercam_ccd",
            "indi_cam90_ccd",
            "indi_canon_ccd",
            "indi_dsi_ccd",
            "indi_ffmv_ccd",
            "indi_fishcamp_ccd",
            "indi_fli_ccd",
            "indi_fuji_ccd",
            "indi_generic_ccd",
            "indi_gphoto_ccd",
            "indi_inovaplx_ccd",
            "indi_kepler_ccd",
            "indi_libcamera_ccd",
            "indi_mallincam_ccd",
            "indi_meadecam_ccd",
            "indi_mi_ccd_eth",
            "indi_mi_ccd_usb",
            "indi_nightscape_ccd",
            "indi_nikon_ccd",
            "indi_nncam_ccd",
            "indi_ogmacam_ccd",
            "indi_omegonprocam_ccd",
            "indi_orion_ssg3_ccd",
            "indi_pentax",
            "indi_pentax_ccd",
            "indi_playerone_ccd",
            "indi_playerone_single_ccd",
            "indi_qhy_ccd",
            "indi_qsi_ccd",
            "indi_sbig_ccd",
            "indi_simulator_ccd",
            "indi_simulator_guide",
            "indi_sony_ccd",
            "indi_starshootg_ccd",
            "indi_svbony_ccd",
            "indi_sx_ccd",
            "indi_toupcam_ccd",
            "indi_tscam_ccd",
            "indi_v4l2_ccd",
            "indi_webcam_ccd",
        )
    }
}
