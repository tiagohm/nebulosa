package nebulosa.fits

/**
 * The Fits extension as defined by Maxim DL.Extension keywords that may be added or read by MaxIm DL, depending on the
 * current equipment and software configuration.
 */
enum class MaxImDLExt : FitsHeader {
    /**
     * if present the image has a valid Bayer color pattern.
     */
    BAYERPAT(ValueType.REAL, "image Bayer color pattern"),

    /**
     * Boltwood Cloud Sensor ambient temperature in degrees C.
     */
    BOLTAMBT(ValueType.REAL, "ambient temperature in degrees C"),

    /**
     * Boltwood Cloud Sensor cloud condition.
     */
    BOLTCLOU(ValueType.REAL, "Boltwood Cloud Sensor cloud condition."),

    /**
     * Boltwood Cloud Sensor daylight level.
     */
    BOLTDAY(ValueType.REAL, "Boltwood Cloud Sensor daylight level."),

    /**
     * Boltwood Cloud Sensor dewpoint in degrees C.
     */
    BOLTDEW(ValueType.REAL, "Boltwood Cloud Sensor dewpoint in degrees C."),

    /**
     * Boltwood Cloud Sensor humidity in percent.
     */
    BOLTHUM(ValueType.REAL, "Boltwood Cloud Sensor humidity in percent."),

    /**
     * Boltwood Cloud Sensor rain condition.
     */
    BOLTRAIN(ValueType.REAL, "Boltwood Cloud Sensor rain condition."),

    /**
     * Boltwood Cloud Sensor sky minus ambient temperature in degrees C.
     */
    BOLTSKYT(ValueType.REAL, "Boltwood Cloud Sensor sky minus ambient temperature in degrees C."),

    /**
     * Boltwood Cloud Sensor wind speed in km/h.
     */
    BOLTWIND(ValueType.REAL, "Boltwood Cloud Sensor wind speed in km/h."),

    /**
     * indicates calibration state of the image; B indicates bias corrected, D indicates dark corrected, F indicates
     * flat corrected.
     */
    CALSTAT(ValueType.REAL, "calibration state of the image"),

    /**
     * type of color sensor Bayer array or zero for monochrome.
     */
    COLORTYP(ValueType.REAL, "type of color sensor"),

    /**
     * initial display screen stretch mode.
     */
    CSTRETCH(ValueType.REAL, "initial display screen stretch mode"),

    /**
     * dark current integration time, if recorded. May be longer than exposure time.
     */
    DARKTIME(ValueType.REAL, "dark current integration time"),

    /**
     * Davis Instruments Weather Station ambient temperature in deg C
     */
    DAVAMBT(ValueType.REAL, "ambient temperature"),

    /**
     * Davis Instruments Weather Station barometric pressure in hPa
     */
    DAVBAROM(ValueType.REAL, "barometric pressure"),

    /**
     * Davis Instruments Weather Station dewpoint in deg C
     */
    DAVDEW(ValueType.REAL, "dewpoint in deg C"),

    /**
     * Davis Instruments Weather Station humidity in percent
     */
    DAVHUM(ValueType.REAL, "humidity in percent"),

    /**
     * Davis Instruments Weather Station solar radiation in W/m^2
     */
    DAVRAD(ValueType.REAL, "solar radiation"),

    /**
     * Davis Instruments Weather Station accumulated rainfall in mm/day
     */
    DAVRAIN(ValueType.REAL, "accumulated rainfall"),

    /**
     * Davis Instruments Weather Station wind speed in km/h
     */
    DAVWIND(ValueType.REAL, "wind speed"),

    /**
     * Davis Instruments Weather Station wind direction in deg
     */
    DAVWINDD(ValueType.REAL, "wind direction"),

    /**
     * status of pier flip for German Equatorial mounts.
     */
    FLIPSTAT(ValueType.REAL, "status of pier flip"),

    /**
     * Focuser position in steps, if focuser is connected.
     */
    FOCUSPOS(ValueType.REAL, "Focuser position in steps"),

    /**
     * Focuser step size in microns, if available.
     */
    FOCUSSZ(ValueType.REAL, "Focuser step size in microns"),

    /**
     * Focuser temperature readout in degrees C, if available.
     */
    FOCUSTEM(ValueType.REAL, "Focuser temperature readout"),

    /**
     * format of file from which image was read.
     */
    INPUTFMT(ValueType.REAL, "format of file"),

    /**
     * ISO camera setting, if camera uses ISO speeds.
     */
    ISOSPEED(ValueType.REAL, "ISO camera setting"),

    /**
     * records the geocentric Julian Day of the start of exposure.
     */
    JD(ValueType.REAL, "geocentric Julian Day"),

    /**
     * records the geocentric Julian Day of the start of exposure.
     */
    JD_GEO(ValueType.REAL, "geocentric Julian Da"),

    /**
     * records the Heliocentric Julian Date at the exposure midpoint.
     */
    JD_HELIO(ValueType.REAL, "Heliocentric Julian Date"),

    /**
     * records the Heliocentric Julian Date at the exposure midpoint.
     */
    JD_HELIO2("JD-HELIO", ValueType.REAL, "Heliocentric Julian Date"),

    /**
     * UT of midpoint of exposure.
     */
    MIDPOINT(ValueType.REAL, "midpoint of exposure"),

    /**
     * user-entered information; free-form notes.
     */
    NOTES(ValueType.REAL, "free-form note"),

    /**
     * nominal altitude of center of image
     */
    OBJCTALT(ValueType.REAL, "altitude of center of image"),

    /**
     * nominal azimuth of center of image
     */
    OBJCTAZ(ValueType.REAL, "nominal azimuth of center of image"),

    /**
     * nominal hour angle of center of image
     */
    OBJCTHA(ValueType.REAL, "nominal hour angle of center of image"),

    /**
     * indicates side-of-pier status when connected to a German Equatorial mount.
     */
    PIERSIDE(ValueType.REAL, "side-of-pier status"),

    /**
     * records the selected Readout Mode (if any) for the camera.
     */
    READOUTM(ValueType.REAL, "Readout Mode for the camera"),

    /**
     * Rotator angle in degrees, if focal plane rotator is connected.
     */
    ROTATANG(ValueType.REAL, "Rotator angle in degrees"),

    /**
     * indicates tile position within a mosaic.
     */
    TILEXY(ValueType.REAL, "tile position within a mosaic"),

    /**
     * X offset of Bayer array on imaging sensor.
     */
    XBAYROFF(ValueType.REAL, "X offset of Bayer array"),

    /**
     * Y offset of Bayer array on imaging sensor.
     */
    YBAYROFF(ValueType.REAL, "Y offset of Bayer array");

    private val header: FitsHeaderImpl

    constructor(key: String, valueType: ValueType, comment: String) {
        header = FitsHeaderImpl(key, hduType, valueType, comment)
    }

    constructor(valueType: ValueType, comment: String) {
        header = FitsHeaderImpl(name, hduType, valueType, comment)
    }

    override val key
        get() = header.key

    override val comment
        get() = header.comment

    override val hduType
        get() = HduType.IMAGE

    override val valueType
        get() = header.valueType

    override fun n(vararg numbers: Int) = header.n(*numbers)
}
