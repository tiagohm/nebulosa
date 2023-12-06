package nebulosa.fits

/**
 * A Set of FITS Standard Extensions for Amateur Astronomical Processing Software Packages published by SBIG.
 */
enum class SBFitsExt : FitsHeader {
    /**
     * Aperture Area of the Telescope used in square millimeters. Note that we are specifying the area as well as the
     * diameter because we want to be able to correct for any central obstruction.
     */
    APTAREA(ValueType.REAL, "Aperture Area of the Telescope"),

    /**
     * Aperture Diameter of the Telescope used in millimeters.
     */
    APTDIA(ValueType.REAL, "Aperture Diameter of the Telescope"),

    /**
     * Upon initial display of this image use this ADU level for the Black level.
     */
    CBLACK(ValueType.INTEGER, "use this ADU level for the Black"),

    /**
     * Temperature of CCD when exposure taken.
     */
    CCD_TEMP("CCD-TEMP", ValueType.REAL, "Temperature of CCD"),

    /**
     * Altitude in degrees of the center of the image in degrees. Format is the same as the OBJCTDEC keyword.
     */
    CENTALT(ValueType.STRING, "Altitude of the center of the image"),

    /**
     * Azimuth in degrees of the center of the image in degrees. Format is the same as the OBJCTDEC keyword.
     */
    CENTAZ(ValueType.STRING, "Azimuth of the center of the image"),

    /**
     * Upon initial display of this image use this ADU level as the White level. For the SBIG method of displaying
     * images using Background and Range the following conversions would be used: Background = CBLACK Range = CWHITE -
     * CBLACK.
     */
    CWHITE(ValueType.INTEGER, "use this ADU level for the White"),

    /**
     * Total dark time of the observation. This is the total time during which dark current is collected by the
     * detector. If the times in the extension are different the primary HDU gives one of the extension times.
     * <p>
     * units = UNITTIME
     * </p>
     * <p>
     * default value = EXPTIME
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DARKTIME(ValueType.REAL, "Dark time"),

    /**
     * Electronic gain in e-/ADU.
     */
    EGAIN(ValueType.REAL, "Electronic gain in e-/ADU"),
    /*
     * Optional Keywords <p> The following Keywords are not defined in the FITS Standard but are defined in this
     * Standard. They may or may not be included by AIP Software Packages adhering to this Standard. Any of these
     * keywords read by an AIP Package must be preserved in files written. </p>
     */
    /**
     * Focal Length of the Telescope used in millimeters.
     */
    FOCALLEN(ValueType.REAL, "Focal Length of the Telescope"),

    /**
     * This indicates the type of image and should be one of the following: Light Frame Dark Frame Bias Frame Flat
     * Field.
     */
    IMAGETYP(ValueType.STRING, "type of image"),

    /**
     * This is the Declination of the center of the image in degrees. The format for this is ‘+25 12 34.111’ (SDD MM
     * SS.SSS) using a space as the separator. For the sign, North is + and South is -.
     */
    OBJCTDEC(ValueType.STRING, "Declination of the center of the image"),

    /**
     * This is the Right Ascension of the center of the image in hours, minutes and secon ds. The format for this is ’12
     * 24 23.123’ (HH MM SS.SSS) using a space as the separator.
     */
    OBJCTRA(ValueType.STRING, "Right Ascension of the center of the image"),

    /**
     * Add this ADU count to each pixel value to get to a zero - based ADU. For example in SBIG images we add 100 ADU to
     * each pixel to stop underflow at Zero ADU from noise. We would set PEDESTAL to - 100 in this case.
     */
    PEDESTAL(ValueType.INTEGER, "ADU count to each pixel value to get to a zero"),

    /**
     * This string indicates the version of this standard that the image was created to ie ‘SBFITSEXT Version 1.0’.
     */
    SBSTDVER(ValueType.STRING, "version of this standard"),

    /**
     * This is the setpoint of the cooling in degrees C. If it is not specified the setpoint is assumed to be the
     */
    SET_TEMP("SET-TEMP", ValueType.REAL, "setpoint of the cooling in degrees C"),

    /**
     * Latitude of the imaging location in degrees. Format is the same as the OBJCTDEC key word.
     */
    SITELAT(ValueType.STRING, "Latitude of the imaging location"),

    /**
     * Longitude of the imaging location in degrees. Format is the same as the OBJCTDEC keyword.
     */
    SITELONG(ValueType.STRING, "Longitude of the imaging location"),

    /**
     * Number of images combined to make this image as in Track and Accumulate or Co - Added images.
     */
    SNAPSHOT(ValueType.INTEGER, "Number of images combined"),

    /**
     * This indicates the name and version of the Software that initially created this file ie ‘SBIGs CCDOps Version
     * 5.10’.
     */
    SWCREATE(ValueType.STRING, "created version of the Software"),

    /**
     * This indicates the name and version of the Software that modified this file ie ‘SBIGs CCDOps Version 5.10’ and
     * the re can be multiple copies of this keyword. Only add this keyword if you actually modified the image and we
     * suggest placing this above the HISTORY keywords corresponding to the modifications made to the image.
     */
    SWMODIFY(ValueType.STRING, "modified version of the Software"),

    /**
     * If the image was auto-guided this is the exposure time in seconds of the tracker used to acquire this image. If
     * this keyword is not present then the image was unguided or hand guided.
     */
    TRAKTIME(ValueType.REAL, "exposure time in seconds of the tracker"),

    /**
     * Binning factor in width.
     */
    XBINNING(ValueType.INTEGER, "Binning factor in width"),

    /**
     * Sub frame X position of upper left pixel relative to whole frame in binned pixel units.
     */
    XORGSUBF(ValueType.INTEGER, "Sub frame X position"),

    /**
     * Pixel width in microns (after binning).
     */
    XPIXSZ(ValueType.REAL, "Pixel width in microns"),

    /**
     * Binning factor in height.
     */
    YBINNING(ValueType.INTEGER, "Binning factor in height"),

    /**
     * Sub frame Y position of upper left pixel relative to whole frame in binned pixel units.
     */
    YORGSUBF(ValueType.INTEGER, "Sub frame Y position"),

    /**
     * Pixel height in microns (after binning).
     */
    YPIXSZ(ValueType.REAL, "Pixel height in microns");

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
