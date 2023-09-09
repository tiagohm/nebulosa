package nebulosa.indi.device.camera

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.imaging.algorithms.CfaPattern
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.indi.protocol.PropertyState
import nebulosa.json.HasJson

interface Camera : GuideOutput, Thermometer, HasJson {

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

    val pixelSizeX: Double

    val pixelSizeY: Double

    fun cooler(enable: Boolean)

    fun dewHeater(enable: Boolean)

    fun temperature(value: Double)

    fun frameFormat(format: String)

    fun frameType(type: FrameType)

    fun frame(x: Int, y: Int, width: Int, height: Int)

    fun bin(x: Int, y: Int)

    fun gain(value: Int)

    fun offset(value: Int)

    fun startCapture(exposureInMicros: Long)

    fun abortCapture()

    override fun writeToJson(gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("name", name)
        gen.writeBooleanField("connected", connected)
        gen.writeBooleanField("exposuring", exposuring)
        gen.writeBooleanField("hasCoolerControl", hasCoolerControl)
        gen.writeNumberField("coolerPower", coolerPower)
        gen.writeBooleanField("cooler", cooler)
        gen.writeBooleanField("hasDewHeater", hasDewHeater)
        gen.writeBooleanField("dewHeater", dewHeater)
        gen.writeObjectField("frameFormats", frameFormats)
        gen.writeBooleanField("canAbort", canAbort)
        gen.writeNumberField("cfaOffsetX", cfaOffsetX)
        gen.writeNumberField("cfaOffsetY", cfaOffsetY)
        gen.writeStringField("cfaType", cfaType.name)
        gen.writeNumberField("exposureMin", exposureMin)
        gen.writeNumberField("exposureMax", exposureMax)
        gen.writeStringField("exposureState", exposureState.name)
        gen.writeNumberField("exposure", exposure)
        gen.writeBooleanField("hasCooler", hasCooler)
        gen.writeBooleanField("canSetTemperature", canSetTemperature)
        gen.writeBooleanField("canSubFrame", canSubFrame)
        gen.writeNumberField("x", x)
        gen.writeNumberField("minX", minX)
        gen.writeNumberField("maxX", maxX)
        gen.writeNumberField("y", y)
        gen.writeNumberField("minY", minY)
        gen.writeNumberField("maxY", maxY)
        gen.writeNumberField("width", width)
        gen.writeNumberField("minWidth", minWidth)
        gen.writeNumberField("maxWidth", maxWidth)
        gen.writeNumberField("height", height)
        gen.writeNumberField("minHeight", minHeight)
        gen.writeNumberField("maxHeight", maxHeight)
        gen.writeBooleanField("canBin", canBin)
        gen.writeNumberField("maxBinX", maxBinX)
        gen.writeNumberField("maxBinY", maxBinY)
        gen.writeNumberField("binX", binX)
        gen.writeNumberField("binY", binY)
        gen.writeNumberField("gain", gain)
        gen.writeNumberField("gainMin", gainMin)
        gen.writeNumberField("gainMax", gainMax)
        gen.writeNumberField("offset", offset)
        gen.writeNumberField("offsetMin", offsetMin)
        gen.writeNumberField("offsetMax", offsetMax)
        gen.writeBooleanField("hasGuiderHead", hasGuiderHead)
        gen.writeNumberField("pixelSizeX", pixelSizeX)
        gen.writeNumberField("pixelSizeY", pixelSizeY)
        gen.writeBooleanField("canPulseGuide", canPulseGuide)
        gen.writeBooleanField("pulseGuiding", pulseGuiding)
        gen.writeBooleanField("hasThermometer", hasThermometer)
        gen.writeNumberField("temperature", temperature)
        gen.writeEndObject()
    }

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
