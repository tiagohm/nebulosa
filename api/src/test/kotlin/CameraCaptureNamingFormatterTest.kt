import io.kotest.matchers.shouldBe
import nebulosa.api.cameras.CameraCaptureNamingFormatter
import nebulosa.api.cameras.CameraCaptureNamingFormatter.Companion.BIAS_FORMAT
import nebulosa.api.cameras.CameraCaptureNamingFormatter.Companion.DARK_FORMAT
import nebulosa.api.cameras.CameraCaptureNamingFormatter.Companion.FLAT_FORMAT
import nebulosa.api.cameras.CameraCaptureNamingFormatter.Companion.LIGHT_FORMAT
import nebulosa.fits.FitsHeader
import nebulosa.fits.FitsKeyword
import nebulosa.image.algorithms.transformation.CfaPattern
import nebulosa.image.format.HeaderCard
import nebulosa.indi.device.Device
import nebulosa.indi.device.MessageSender
import nebulosa.indi.device.PropertyVector
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.mount.*
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.PropertyState
import nebulosa.math.*
import nebulosa.time.SystemClock
import org.junit.jupiter.api.Test
import java.time.*
import java.util.*

class CameraCaptureNamingFormatterTest {

    private val header = FitsHeader()

    init {
        header.add("FRAME", "Light")
        header.add(FitsKeyword.EXPTIME, 1.5)
        header.add(FitsKeyword.FILTER, "Red")
        header.add(FitsKeyword.GAIN, 80)
        header.add(FitsKeyword.XBINNING, 2)
        header.add(FitsKeyword.NAXIS1, 1280)
        header.add(FitsKeyword.NAXIS2, 1024)
        header.add(FitsKeyword.CCD_TEMP, -15.0)
        header.add(FitsKeyword.RA, "06 45 08.91728".hours.toDegrees)
        header.add(FitsKeyword.DEC, "-16 42 58.0171".deg.toDegrees)
    }

    @Test
    fun type() {
        FORMATTER.format("[type]", header) shouldBe "LIGHT"
    }

    @Test
    fun year() {
        FORMATTER.format("[year]", header) shouldBe "2024"
        FORMATTER.format("[year:2]", header) shouldBe "24"
        FORMATTER.format("[year:4]", header) shouldBe "2024"
        FORMATTER.format("[year:3]", header) shouldBe "2024"
    }

    @Test
    fun month() {
        FORMATTER.format("[month]", header) shouldBe "07"
    }

    @Test
    fun day() {
        FORMATTER.format("[day]", header) shouldBe "04"
    }

    @Test
    fun hour() {
        FORMATTER.format("[hour]", header) shouldBe "17"
    }

    @Test
    fun minute() {
        FORMATTER.format("[min]", header) shouldBe "37"
        FORMATTER.format("[minute]", header) shouldBe "37"
    }

    @Test
    fun second() {
        FORMATTER.format("[sec]", header) shouldBe "36"
        FORMATTER.format("[second]", header) shouldBe "36"
    }

    @Test
    fun millisecond() {
        FORMATTER.format("[ms]", header) shouldBe "369"
    }

    @Test
    fun exposureTime() {
        FORMATTER.format("[exp]", header) shouldBe "1500000"
        FORMATTER.format("[exp:s]", header) shouldBe "1s"
        FORMATTER.format("[exp:ms]", header) shouldBe "1500ms"
        FORMATTER.format("[exp:us]", header) shouldBe "1500000"
        FORMATTER.format("[exposure]", header) shouldBe "1500000"
    }

    @Test
    fun filter() {
        FORMATTER.format("[filter]", header) shouldBe "Red"
    }

    @Test
    fun gain() {
        FORMATTER.format("[gain]", header) shouldBe "80"
    }

    @Test
    fun bin() {
        FORMATTER.format("[bin]", header) shouldBe "2"
    }

    @Test
    fun width() {
        FORMATTER.format("[width]", header) shouldBe "1280"
        FORMATTER.format("[w]", header) shouldBe "1280"
    }

    @Test
    fun height() {
        FORMATTER.format("[height]", header) shouldBe "1024"
        FORMATTER.format("[h]", header) shouldBe "1024"
    }

    @Test
    fun temperature() {
        FORMATTER.format("[temp]", header) shouldBe "-15"
        FORMATTER.format("[temperature]", header) shouldBe "-15"
    }

    @Test
    fun rightAscension() {
        FORMATTER.format("[ra]", header) shouldBe "06h45m09s"
    }

    @Test
    fun declination() {
        FORMATTER.format("[dec]", header) shouldBe "-016d42m58s"
    }

    @Test
    fun camera() {
        FORMATTER.format("[camera]", header) shouldBe "Camera Simulator"
    }

    @Test
    fun mount() {
        FORMATTER.format("[mount]", header) shouldBe "Mount Simulator"
    }

    @Test
    fun focuser() {
        FORMATTER.format("[focuser]", header) shouldBe "Focuser Simulator"
    }

    @Test
    fun wheel() {
        FORMATTER.format("[wheel]", header) shouldBe "Wheel Simulator"
    }

    @Test
    fun rotator() {
        FORMATTER.format("[rotator]", header) shouldBe "Rotator Simulator"
    }

    @Test
    fun n() {
        FORMATTER.format("[n]", header) shouldBe "0001"
        FORMATTER.format("[n:1]", header) shouldBe "2"
        FORMATTER.format("[n:2]", header) shouldBe "03"
        FORMATTER.format("[n:3]", header) shouldBe "004"
        FORMATTER.format("[n:6]", header) shouldBe "000005"
    }

    @Test
    fun light() {
        with(header) {
            add("FRAME", "Light")
            FORMATTER.format(LIGHT_FORMAT, this) shouldBe "Camera Simulator_LIGHT_240704173736369_Red_1280_1024_1500000_2_80"
        }
    }

    @Test
    fun dark() {
        with(header) {
            add("FRAME", "Dark")
            FORMATTER.format(DARK_FORMAT, this) shouldBe "Camera Simulator_DARK_1280_1024_1500000_2_80"
        }
    }

    @Test
    fun flat() {
        with(header) {
            add("FRAME", "Flat")
            FORMATTER.format(FLAT_FORMAT, this) shouldBe "Camera Simulator_FLAT_Red_1280_1024_2"
        }
    }

    @Test
    fun bias() {
        with(header) {
            add("FRAME", "Bias")
            FORMATTER.format(BIAS_FORMAT, this) shouldBe "Camera Simulator_BIAS_1280_1024_2_80"
        }
    }

    @Test
    fun unknown() {
        FORMATTER.format("[abc]_[camera]_[123]", header) shouldBe "abc_Camera Simulator_123"
    }

    @Test
    fun notFound() {
        with(header) {
            delete(FitsKeyword.RA)
            FORMATTER.format("[ra]", this) shouldBe ""
            FORMATTER.format("[ra]_[ra]", this) shouldBe "_"
        }
    }

    @Test
    fun illegalChars() {
        FORMATTER.format("[???]", header) shouldBe "[]"
        FORMATTER.format("[???a]", header) shouldBe "[a]"
        FORMATTER.format("[a???]", header) shouldBe "[a]"
        FORMATTER.format("[][/\\:*?\"<>|]", header) shouldBe "[][]"
    }

    @Suppress("LeakingThis")
    private abstract class Simulator(override val name: String) : Device, MessageSender {

        final override val sender = this
        final override val id = UUID.randomUUID().toString()
        final override val connected = true
        final override val properties = emptyMap<String, PropertyVector<*, *>>()
        final override val messages = emptyList<String>()
        final override val snoopedDevices = emptyList<Device>()

        final override fun sendMessageToServer(message: INDIProtocol) = Unit

        final override fun snoop(devices: Iterable<Device?>) = Unit

        final override fun handleMessage(message: INDIProtocol) = Unit

        final override fun close() = Unit

        final override fun connect() = Unit

        final override fun disconnect() = Unit
    }

    private data object CameraSim : Simulator("Camera Simulator"), Camera {

        override val exposuring = true
        override val hasCoolerControl = true
        override val coolerPower = 0.0
        override val cooler = true
        override val hasDewHeater = true
        override val dewHeater = true
        override val frameFormats = listOf("RAW16")
        override val canAbort = true
        override val cfaOffsetX = 0
        override val cfaOffsetY = 0
        override val cfaType = CfaPattern.RGGB
        override val exposureMin: Duration = Duration.ofNanos(1000)
        override val exposureMax: Duration = Duration.ofMinutes(10)
        override val exposureState = PropertyState.IDLE
        override val exposureTime: Duration = Duration.ofSeconds(1)
        override val hasCooler = true
        override val canSetTemperature = true
        override val canSubFrame = true
        override val x = 0
        override val minX = 0
        override val maxX = 1279
        override val y = 0
        override val minY = 0
        override val maxY = 1023
        override val width = 0
        override val minWidth = 0
        override val maxWidth = 1280
        override val height = 0
        override val minHeight = 0
        override val maxHeight = 1024
        override val canBin = true
        override val maxBinX = 4
        override val maxBinY = 4
        override val binX = 1
        override val binY = 1
        override val gain = 0
        override val gainMin = 0
        override val gainMax = 100
        override val offset = 0
        override val offsetMin = 0
        override val offsetMax = 100
        override val guideHead = null
        override val pixelSizeX = 2.8
        override val pixelSizeY = 2.8
        override val canPulseGuide = true
        override val pulseGuiding = true
        override val hasThermometer = true
        override val temperature = 15.0

        override fun cooler(enabled: Boolean) = Unit

        override fun dewHeater(enabled: Boolean) = Unit

        override fun temperature(value: Double) = Unit

        override fun frameFormat(format: String?) = Unit

        override fun frameType(type: FrameType) = Unit

        override fun frame(x: Int, y: Int, width: Int, height: Int) = Unit

        override fun bin(x: Int, y: Int) = Unit

        override fun gain(value: Int) = Unit

        override fun offset(value: Int) = Unit

        override fun startCapture(exposureTime: Duration) = Unit

        override fun abortCapture() = Unit

        override fun fitsKeywords(vararg cards: HeaderCard) = Unit

        override fun guideNorth(duration: Duration) = Unit

        override fun guideSouth(duration: Duration) = Unit

        override fun guideEast(duration: Duration) = Unit

        override fun guideWest(duration: Duration) = Unit
    }

    private data object MountSim : Simulator("Mount Simulator"), Mount {

        override val slewing = false
        override val tracking = true
        override val canSync = true
        override val canGoTo = true
        override val canAbort = true
        override val canHome = true
        override val slewRates = emptyList<SlewRate>()
        override val slewRate = null
        override val mountType = MountType.EQ_GEM
        override val trackModes = TrackMode.entries
        override val trackMode = TrackMode.SIDEREAL
        override val pierSide = PierSide.WEST
        override val guideRateWE = 0.0
        override val guideRateNS = 0.0
        override val rightAscension = 0.0
        override val declination = 0.0
        override val hasGPS = true
        override val longitude = 0.0
        override val latitude = 0.0
        override val elevation = 0.0
        override val dateTime: OffsetDateTime = OffsetDateTime.now(SystemClock)
        override val canPark = true
        override val parking = false
        override val parked = false
        override val canPulseGuide = true
        override val pulseGuiding = true

        override fun tracking(enable: Boolean) = Unit

        override fun sync(ra: Angle, dec: Angle) = Unit

        override fun syncJ2000(ra: Angle, dec: Angle) = Unit

        override fun slewTo(ra: Angle, dec: Angle) = Unit

        override fun slewToJ2000(ra: Angle, dec: Angle) = Unit

        override fun goTo(ra: Angle, dec: Angle) = Unit

        override fun goToJ2000(ra: Angle, dec: Angle) = Unit

        override fun home() = Unit

        override fun abortMotion() = Unit

        override fun trackMode(mode: TrackMode) = Unit

        override fun slewRate(rate: SlewRate) = Unit

        override fun moveNorth(enabled: Boolean) = Unit

        override fun moveSouth(enabled: Boolean) = Unit

        override fun moveWest(enabled: Boolean) = Unit

        override fun moveEast(enabled: Boolean) = Unit

        override fun coordinates(longitude: Angle, latitude: Angle, elevation: Distance) = Unit

        override fun dateTime(dateTime: OffsetDateTime) = Unit

        override fun park() = Unit

        override fun unpark() = Unit

        override fun guideNorth(duration: Duration) = Unit

        override fun guideSouth(duration: Duration) = Unit

        override fun guideEast(duration: Duration) = Unit

        override fun guideWest(duration: Duration) = Unit
    }

    private data object FocuserSim : Simulator("Focuser Simulator"), Focuser {

        override val moving = false
        override val canSync = true
        override val canAbort = true
        override val position = 1
        override val canAbsoluteMove = true
        override val canRelativeMove = true
        override val canReverse = true
        override val reversed = true
        override val hasBacklash = true
        override val maxPosition = 100000
        override val hasThermometer = true
        override val temperature = 15.0

        override fun moveFocusIn(steps: Int) = Unit

        override fun moveFocusOut(steps: Int) = Unit

        override fun moveFocusTo(steps: Int) = Unit

        override fun abortFocus() = Unit

        override fun reverseFocus(enable: Boolean) = Unit

        override fun syncFocusTo(steps: Int) = Unit
    }

    private data object WheelSim : Simulator("Wheel Simulator"), FilterWheel {

        override val moving = false
        override val count = 5
        override val names = listOf("Luminance", "Red", "Green", "Blue", "Dark")
        override val position = 1

        override fun moveTo(position: Int) = Unit

        override fun names(names: Iterable<String>) = Unit
    }

    private data object RotatorSim : Simulator("Rotator Simulator"), Rotator {

        override val moving = false
        override val canSync = true
        override val canAbort = true
        override val canReverse = true
        override val canHome = true
        override val hasBacklashCompensation = false
        override val backslash = 0
        override val angle = 0.0
        override val minAngle = 0.0
        override val maxAngle = 360.0
        override val reversed = false

        override fun moveRotator(angle: Double) = Unit

        override fun syncRotator(angle: Double) = Unit

        override fun homeRotator() = Unit

        override fun reverseRotator(enable: Boolean) = Unit

        override fun abortRotator() = Unit
    }

    companion object {

        @JvmStatic private val CLOCK = Clock.fixed(Instant.ofEpochSecond(1720114656, 369000000), ZoneOffset.UTC)
        @JvmStatic private val FORMATTER = CameraCaptureNamingFormatter(CameraSim, MountSim, WheelSim, FocuserSim, RotatorSim, CLOCK)
    }
}
