package nebulosa.fits

@Suppress("EnumEntryName")
enum class FitsKeyword : FitsHeaderKey {

    // Standard.
    // https://github.com/nom-tam-fits/nom-tam-fits/blob/master/src/main/java/nom/tam/fits/header/Standard.java
    // https://github.com/nom-tam-fits/nom-tam-fits/blob/master/src/main/java/nom/tam/fits/header/ObservationDescription.java
    // https://github.com/nom-tam-fits/nom-tam-fits/blob/master/src/main/java/nom/tam/fits/header/ObservationDurationDescription.java

    /**
     * The value field shall contain a character string identifying who compiled the information in the data associated
     * with the key. This keyword is appropriate when the data originate in a published paper or are compiled from many
     * sources.
     */
    AUTHOR(HduType.ANY, ValueType.STRING, "author of the data"),

    /**
     * The value field shall contain an integer. The absolute value is used in computing the sizes of data structures.
     * It shall specify the number of bits that represent a data value. RANGE: -64,-32,8,16,32
     */
    BITPIX(HduType.ANY, ValueType.INTEGER, "bits per data value"),

    /**
     * This keyword shall be used only in primary array headers or IMAGE extension headers with positive values of
     * BITPIX (i.e., in arrays with integer data). Columns 1-8 contain the string, `BLANK ' (ASCII blanks in columns
     * 6-8). The value field shall contain an integer that specifies the representation of array values whose physical
     * values are undefined.
     */
    BLANK(HduType.IMAGE, ValueType.INTEGER, "value used for undefined array elements"),

    /**
     * Columns 1-8 contain ASCII blanks. This keyword has no associated value. Columns 9-80 may contain any ASCII text.
     * Any number of card images with blank keyword fields may appear in a key.
     */
    BLANKS("        ", HduType.ANY, ValueType.NONE, ""),

    /**
     * This keyword may be used only in the primary key. It shall appear within the first 36 card images of the FITS
     * file. (Note: This keyword thus cannot appear if NAXIS is greater than 31, or if NAXIS is greater than 30 and the
     * EXTEND keyword is present.) Its presence with the required logical value of T advises that the physical block
     * size of the FITS file on which it appears may be an integral multiple of the logical record length, and not
     * necessarily equal to it. Physical block size and logical record length may be equal even if this keyword is
     * present or unequal if it is absent. It is reserved primarily to prevent its use with other meanings. Since the
     * issuance of version 1 of the standard, the BLOCKED keyword has been deprecated.
     */
    @Deprecated("no blocksize other that 2880 may be used")
    BLOCKED(HduType.PRIMARY, ValueType.LOGICAL, "is physical blocksize a multiple of 2880?"),

    /**
     * This keyword shall be used, along with the BZERO keyword, when the array pixel values are not the true physical
     * values, to transform the primary data array values to the true physical values they represent, using the
     * equation: physical_value = BZERO + BSCALE * array_value. The value field shall contain a floating point number
     * representing the coefficient of the linear term in the scaling equation, the ratio of physical value to array
     * value at zero offset. The default value for this keyword is 1.0.
     */
    BSCALE(HduType.IMAGE, ValueType.REAL, "linear factor in scaling equation"),

    /**
     * The value field shall contain a character string, describing the physical units in which the quantities in the
     * array, after application of BSCALE and BZERO, are expressed. The units of all FITS key keyword values, with the
     * exception of measurements of angles, should conform with the recommendations in the IAU Style Manual. For angular
     * measurements given as floating point values and specified with reserved keywords, degrees are the recommended
     * units (with the units, if specified, given as 'deg').
     */
    BUNIT(HduType.IMAGE, ValueType.STRING, "physical units of the array values"),

    /**
     * This keyword shall be used, along with the BSCALE keyword, when the array pixel values are not the true physical
     * values, to transform the primary data array values to the true values using the equation: physical_value = BZERO
     * + BSCALE * array_value. The value field shall contain a floating point number representing the physical value
     * corresponding to an array value of zero. The default value for this keyword is 0.0.
     */
    BZERO(HduType.IMAGE, ValueType.REAL, "zero point in scaling equation"),

    /**
     * The value field shall contain a floating point number giving the partial derivative of the coordinate specified
     * by the CTYPEn keywords with respect to the pixel index, evaluated at the reference point CRPIXn, in units of the
     * coordinate specified by the CTYPEn keyword. These units must follow the prescriptions of section 5.3 of the FITS
     * Standard.
     */
    CDELTn(HduType.IMAGE, ValueType.REAL, "coordinate increment along axis"),

    /**
     * This keyword shall have no associated value; columns 9-80 may contain any ASCII text. Any number of COMMENT card
     * images may appear in a key.
     */
    COMMENT(HduType.ANY, ValueType.NONE, ""),

    /**
     * The CONTINUE keyword, when followed by spaces in columns 9 and 10 of the card image and a character string
     * enclosed in single quotes starting in column 11 or higher, indicates that the quoted string should be treated as
     * a continuation of the character string value in the previous key keyword. To conform to this convention, the
     * character string value on the previous keyword must end with the ampersand character ('&amp;'), but the ampersand
     * is not part of the value string and should be deleted before concatenating the strings together. The character
     * string value may be continued on any number of consecutive CONTINUE keywords, thus effectively allowing
     * arbitrarily long strings to be written as keyword values.
     */
    CONTINUE(HduType.ANY, ValueType.NONE, "denotes the CONTINUE long string keyword convention"),

    /**
     * This keyword is used to indicate a rotation from a standard coordinate system described by the CTYPEn to a
     * different coordinate system in which the values in the array are actually expressed. Rules for such rotations are
     * not further specified in the Standard; the rotation should be explained in comments. The value field shall
     * contain a floating point number giving the rotation angle in degrees between axis n and the direction implied by
     * the coordinate system defined by CTYPEn. In unit degrees.
     */
    CROTAn(HduType.IMAGE, ValueType.REAL, "coordinate system rotation angle"),

    /**
     * The value field shall contain a floating point number, identifying the location of a reference point along axis
     * n, in units of the axis index. This value is based upon a counter that runs from 1 to NAXISn with an increment of
     * 1 per pixel. The reference point value need not be that for the center of a pixel nor lie within the actual data
     * array. Use comments to indicate the location of the index point relative to the pixel.
     */
    CRPIXn(HduType.IMAGE, ValueType.REAL, "coordinate system reference pixel"),

    /**
     * The value field shall contain a floating point number, giving the value of the coordinate specified by the CTYPEn
     * keyword at the reference point CRPIXn. Units must follow the prescriptions of section 5.3 of the FITS Standard.
     */
    CRVALn(HduType.IMAGE, ValueType.REAL, "coordinate system value at reference pixel"),

    /**
     * The value field shall contain a character string, giving the name of the coordinate represented by axis n.
     */
    CTYPEn(HduType.IMAGE, ValueType.STRING, "name of the coordinate axis"),

    /**
     * The value field shall always contain a floating point number, regardless of the value of BITPIX. This number
     * shall give the maximum valid physical value represented by the array, exclusive of any special values.
     */
    DATAMAX(HduType.IMAGE, ValueType.REAL, "maximum data value"),

    /**
     * The value field shall always contain a floating point number, regardless of the value of BITPIX. This number
     * shall give the minimum valid physical value represented by the array, exclusive of any special values.
     */
    DATAMIN(HduType.IMAGE, ValueType.REAL, "minimum data value"),

    /**
     * The date on which the HDU was created, in the format specified in the FITS Standard. The old date format was
     * 'yy/mm/dd' and may be used only for dates from 1900 through 1999. the new Y2K compliant date format is
     * 'yyyy-mm-dd' or 'yyyy-mm-ddTHH:MM:SS[.sss]'.
     */
    DATE(HduType.ANY, ValueType.STRING, "date of file creation"),

    /**
     * The date of the observation, in the format specified in the FITS Standard. The old date format was 'yy/mm/dd' and
     * may be used only for dates from 1900 through 1999. The new Y2K compliant date format is 'yyyy-mm-dd' or
     * 'yyyy-mm-ddTHH:MM:SS[.sss]'.
     */
    DATE_OBS("DATE-OBS", HduType.ANY, ValueType.STRING, "date of the observation"),

    /**
     * This keyword has no associated value. Columns 9-80 shall be filled with ASCII blanks.
     */
    END(HduType.ANY, ValueType.NONE, ""),

    /**
     * The value field shall contain a floating point number giving the equinox in years for the celestial coordinate
     * system in which positions are expressed. Starting with Version 1, the Standard has deprecated the use of the
     * EPOCH keyword and thus it shall not be used in FITS files created after the adoption of the standard; rather, the
     * EQUINOX keyword shall be used.
     */
    @Deprecated("use EQUINOX instead")
    EPOCH(HduType.ANY, ValueType.REAL, "equinox of celestial coordinate system"),

    /**
     * The value field shall contain a floating point number giving the equinox in years for the celestial coordinate
     * system in which positions are expressed.
     */
    EQUINOX(HduType.ANY, ValueType.REAL, "equinox of celestial coordinate system"),

    /**
     * If the FITS file may contain extensions, a card image with the keyword EXTEND and the value field containing the
     * logical value T must appear in the primary key immediately after the last NAXISn card image, or, if NAXIS=0, the
     * NAXIS card image. The presence of this keyword with the value T in the primary key does not require that
     * extensions be present.
     */
    EXTEND(HduType.PRIMARY, ValueType.LOGICAL, "may the FITS file contain extensions?"),

    /**
     * The value field shall contain an integer, specifying the level in a hierarchy of extension levels of the
     * extension key containing it. The value shall be 1 for the highest level; levels with a higher value of this
     * keyword shall be subordinate to levels with a lower value. If the EXTLEVEL keyword is absent, the file should be
     * treated as if the value were 1. This keyword is used to describe an extension and should not appear in the
     * primary key.RANGE: [1:] DEFAULT: 1
     */
    EXTLEVEL(HduType.EXTENSION, ValueType.INTEGER, "hierarchical level of the extension"),

    /**
     * The value field shall contain a character string, to be used to distinguish among different extensions of the
     * same type, i.e., with the same value of XTENSION, in a FITS file. This keyword is used to describe an extension
     * and should not appear in the primary key.
     */
    EXTNAME(HduType.EXTENSION, ValueType.STRING, "name of the extension"),

    /**
     * The value field shall contain an integer, to be used to distinguish among different extensions in a FITS file
     * with the same type and name, i.e., the same values for XTENSION and EXTNAME. The values need not start with 1 for
     * the first extension with a particular value of EXTNAME and need not be in sequence for subsequent values. If the
     * EXTVER keyword is absent, the file should be treated as if the value were 1. This keyword is used to describe an
     * extension and should not appear in the primary key.RANGE: [1:] DEFAULT: 1
     */
    EXTVER(HduType.EXTENSION, ValueType.INTEGER, "version of the extension"),

    /**
     * The value field shall contain an integer that shall be used in any way appropriate to define the data structure,
     * consistent with Eq. 5.2 in the FITS Standard. This keyword originated for use in FITS Random Groups where it
     * specifies the number of random groups present. In most other cases this keyword will have the value 1.
     */
    GCOUNT(HduType.EXTENSION, ValueType.INTEGER, "group count"),

    /**
     * The value field shall contain the logical constant T. The value T associated with this keyword implies that
     * random groups records are present.
     */
    GROUPS(HduType.GROUPS, ValueType.LOGICAL, "indicates random groups structure"),

    /**
     * This keyword shall have no associated value; columns 9-80 may contain any ASCII text. The text should contain a
     * history of steps and procedures associated with the processing of the associated data. Any number of HISTORY card
     * images may appear in a key.
     */
    HISTORY(HduType.ANY, ValueType.NONE, "processing history of the data"),

    /**
     * The value field shall contain a character string identifying the instrument used to acquire the data associated
     * with the key.
     */
    INSTRUME(HduType.ANY, ValueType.STRING, "name of instrument"),

    /**
     * The value field shall contain a non-negative integer no greater than 999, representing the number of axes in the
     * associated data array. A value of zero signifies that no data follow the key in the HduType. In the context of FITS
     * 'TABLE' or 'BINTABLE' extensions, the value of NAXIS is always 2.RANGE: [0:999]
     */
    NAXIS(HduType.ANY, ValueType.INTEGER, "number of axes"),

    /**
     * The value field of this indexed keyword shall contain a non-negative integer, representing the number of elements
     * along axis n of a data array. The NAXISn must be present for all values n = 1,...,NAXIS, and for no other values
     * of n. A value of zero for any of the NAXISn signifies that no data follow the key in the HduType. If NAXIS is equal
     * to 0, there should not be any NAXISn keywords.RANGE: [0:]
     */
    NAXISn(HduType.ANY, ValueType.INTEGER, "size of the n'th axis"),

    /**
     * The value field shall contain a character string giving a name for the object observed.
     */
    OBJECT(HduType.ANY, ValueType.STRING, "name of observed object"),

    /**
     * The value field shall contain a character string identifying who acquired the data associated with the key.
     */
    OBSERVER(HduType.ANY, ValueType.STRING, "observer who acquired the data"),

    /**
     * The value field shall contain a character string identifying the organization or institution responsible for
     * creating the FITS file.
     */
    ORIGIN(HduType.ANY, ValueType.STRING, "organization responsible for the data"),

    /**
     * The value field shall contain an integer that shall be used in any way appropriate to define the data structure,
     * consistent with Eq. 5.2 in the FITS Standard. This keyword was originated for use with FITS Random Groups and
     * represented the number of parameters preceding each group. It has since been used in 'BINTABLE' extensions to
     * represent the size of the data heap following the main data table. In most other cases its value will be zero.
     */
    PCOUNT(HduType.EXTENSION, ValueType.INTEGER, "parameter count"),

    /**
     * This keyword is reserved for use within the FITS Random Groups structure. This keyword shall be used, along with
     * the PZEROn keyword, when the nth FITS group parameter value is not the true physical value, to transform the
     * group parameter value to the true physical values it represents, using the equation, physical_value = PZEROn +
     * PSCALn * group_parameter_value. The value field shall contain a floating point number representing the
     * coefficient of the linear term, the scaling factor between true values and group parameter values at zero offset.
     * The default value for this keyword is 1.0.
     */
    PSCALn(HduType.GROUPS, ValueType.REAL, "parameter scaling factor"),

    /**
     * This keyword is reserved for use within the FITS Random Groups structure. The value field shall contain a
     * character string giving the name of parameter n. If the PTYPEn keywords for more than one value of n have the
     * same associated name in the value field, then the data value for the parameter of that name is to be obtained by
     * adding the derived data values of the corresponding parameters. This rule provides a mechanism by which a random
     * parameter may have more precision than the accompanying data array elements; for example, by summing two 16-bit
     * values with the first scaled relative to the other such that the sum forms a number of up to 32-bit precision.
     */
    PTYPEn(HduType.GROUPS, ValueType.STRING, "name of random groups parameter"),

    /**
     * This keyword is reserved for use within the FITS Random Groups structure. This keyword shall be used, along with
     * the PSCALn keyword, when the nth FITS group parameter value is not the true physical value, to transform the
     * group parameter value to the physical value. The value field shall contain a floating point number, representing
     * the true value corresponding to a group parameter value of zero. The default value for this keyword is 0.0. The
     * transformation equation is as follows: physical_value = PZEROn + PSCALn * group_parameter_value.DEFAULT: 0.0
     */
    PZEROn(HduType.GROUPS, ValueType.REAL, "parameter scaling zero point"),

    /**
     * Coordinate reference frame of major/minor axes.If absent the default value is 'FK5'.
     */
    RADESYS(HduType.ANY, ValueType.STRING, "Coordinate reference frame of major/minor axes."),

    /**
     * The value field shall contain a character string citing a reference where the data associated with the key are
     * published.
     */
    REFERENC(HduType.ANY, ValueType.STRING, "bibliographic reference"),

    /**
     * The SIMPLE keyword is required to be the first keyword in the primary key of all FITS files. The value field
     * shall contain a logical constant with the value T if the file conforms to the standard. This keyword is mandatory
     * for the primary key and is not permitted in extension headers. A value of F signifies that the file does not
     * conform to this standard.
     */
    SIMPLE(HduType.PRIMARY, ValueType.LOGICAL, "does file conform to the Standard?"),

    /**
     * The value field of this indexed keyword shall contain an integer specifying the column in which field n starts in
     * an ASCII TABLE extension. The first column of a row is numbered 1.RANGE: [1:]
     */
    TBCOLn(HduType.ASCII_TABLE, ValueType.INTEGER, "begining column number"),

    /**
     * The value field of this indexed keyword shall contain a character string describing how to interpret the contents
     * of field n as a multidimensional array, providing the number of dimensions and the length along each axis. The
     * form of the value is not further specified by the Standard. A proposed convention is described in Appendix B.2 of
     * the FITS Standard in which the value string has the format '(l,m,n...)' where l, m, n,... are the dimensions of
     * the array.
     */
    TDIMn(HduType.BINTABLE, ValueType.STRING, "dimensionality of the array "),

    /**
     * The value field of this indexed keyword shall contain a character string describing the format recommended for
     * the display of the contents of field n. If the table value has been scaled, the physical value shall be
     * displayed. All elements in a field shall be displayed with a single, repeated format. For purposes of display,
     * each byte of bit (type X) and byte (type B) arrays is treated as an unsigned integer. Arrays of type A may be
     * terminated with a zero byte. Only the format codes in Table 8.6, discussed in section 8.3.4 of the FITS Standard,
     * are permitted for encoding. The format codes must be specified in upper case. If the Bw.m, Ow.m, and Zw.m formats
     * are not readily available to the reader, the Iw.m display format may be used instead, and if the ENw.d and ESw.d
     * formats are not available, Ew.d may be used. The meaning of this keyword is not defined for fields of type P in
     * the Standard but may be defined in conventions using such fields.
     */
    TDISPn(HduType.TABLE, ValueType.STRING, "display format"),

    /**
     * The value field shall contain a character string identifying the telescope used to acquire the data associated
     * with the key.
     */
    TELESCOP(HduType.ANY, ValueType.STRING, "name of telescope"),

    /**
     * The value field shall contain a non-negative integer representing the number of fields in each row of a 'TABLE'
     * or 'BINTABLE' extension. The maximum permissible value is 999. RANGE: [0:999]
     */
    TFIELDS(HduType.TABLE, ValueType.INTEGER, "number of columns in the table"),

    /**
     * The value field of this indexed keyword shall contain a character string describing the format in which field n
     * is encoded in a 'TABLE' or 'BINTABLE' extension.
     */
    TFORMn(HduType.TABLE, ValueType.STRING, "column data format"),

    /**
     * The value field of this keyword shall contain an integer providing the separation, in bytes, between the start of
     * the main data table and the start of a supplemental data area called the heap. The default value shall be the
     * product of the values of NAXIS1 and NAXIS2. This keyword shall not be used if the value of PCOUNT is zero. A
     * proposed application of this keyword is presented in Appendix B.1 of the FITS Standard.
     */
    THEAP(HduType.BINTABLE, ValueType.INTEGER, "offset to starting data heap address"),

    /**
     * In ASCII 'TABLE' extensions, the value field for this indexed keyword shall contain the character string that
     * represents an undefined value for field n. The string is implicitly blank filled to the width of the field. In
     * binary 'BINTABLE' table extensions, the value field for this indexed keyword shall contain the integer that
     * represents an undefined value for field n of data type B, I, or J. The keyword may not be used in 'BINTABLE'
     * extensions if field n is of any other data type.
     */
    TNULLn(HduType.TABLE, ValueType.STRING, "value used to indicate undefined table element"),

    /**
     * This indexed keyword shall be used, along with the TZEROn keyword, when the quantity in field n does not
     * represent a true physical quantity. The value field shall contain a floating point number representing the
     * coefficient of the linear term in the equation, physical_value = TZEROn + TSCALn * field_value, which must be
     * used to compute the true physical value of the field, or, in the case of the complex data types C and M, of the
     * real part of the field with the imaginary part of the scaling factor set to zero. The default value for this
     * keyword is 1.0. This keyword may not be used if the format of field n is A, L, or X.DEFAULT: 1.0
     */
    TSCALn(HduType.TABLE, ValueType.REAL, "linear data scaling factor"),

    /**
     * The value field for this indexed keyword shall contain a character string, giving the name of field n. It is
     * recommended that only letters, digits, and underscore (hexadecimal code 5F, ('_') be used in the name. String
     * comparisons with the values of TTYPEn keywords should not be case sensitive. The use of identical names for
     * different fields should be avoided.
     */
    TTYPEn(HduType.TABLE, ValueType.STRING, "column name"),

    /**
     * The value field shall contain a character string describing the physical units in which the quantity in field n,
     * after any application of TSCALn and TZEROn, is expressed. The units of all FITS key keyword values, with the
     * exception of measurements of angles, should conform with the recommendations in the IAU Style Manual. For angular
     * measurements given as floating point values and specified with reserved keywords, degrees are the recommended
     * units (with the units, if specified, given as 'deg').
     */
    TUNITn(HduType.TABLE, ValueType.STRING, "column units"),

    /**
     * This indexed keyword shall be used, along with the TSCALn keyword, when the quantity in field n does not
     * represent a true physical quantity. The value field shall contain a floating point number representing the true
     * physical value corresponding to a value of zero in field n of the FITS file, or, in the case of the complex data
     * types C and M, in the real part of the field, with the imaginary part set to zero. The default value for this
     * keyword is 0.0. This keyword may not be used if the format of field n is A, L, or X.DEFAULT: 0.0
     */
    TZEROn(HduType.TABLE, ValueType.REAL, "column scaling zero point"),

    /**
     * The value field shall contain a character string giving the name of the extension type. This keyword is mandatory
     * for an extension key and must not appear in the primary key. For an extension that is not a standard extension,
     * the type name must not be the same as that of a standard extension.
     */
    XTENSION(HduType.EXTENSION, ValueType.STRING, "marks beginning of new HDU"),

    // FITS keywords that have been widely used within the astronomical community.
    // These are the Keywords that describe the observation.

    /**
     * The value field shall contain a floating point number giving the air mass during the observation by a ground
     * based telescope. The value of the airmass is often approximated by the secant of the elevation angle and has a
     * value of 1.0 at the zenith and increases towards the horizon. This value is assumed to correspond to the start of
     * the observation unless another interpretation is clearly explained in the comment field.
     */
    AIRMASS(HduType.ANY, ValueType.REAL, "air mass"),

    /**
     * The value field gives the declination of the observation. It may be expressed either as a floating point number
     * in units of decimal degrees, or as a character string in 'dd:mm:ss.sss' format where the decimal point and number
     * of fractional digits are optional. The coordinate reference frame is given by the RADECSYS keyword, and the
     * coordinate epoch is given by the EQUINOX keyword. Example: -47.25944 or '-47:15:34.00'.
     */
    DEC(HduType.ANY, ValueType.STRING, "declination of the observed object"),

    /**
     * The value field shall contain a floating point number giving the nominal declination of the pointing direction in
     * units of decimal degrees. The coordinate reference frame is given by the RADECSYS keyword, and the coordinate
     * epoch is given by the EQUINOX keyword. The precise definition of this keyword is instrument-specific, but
     * typically the nominal direction corresponds to the direction to which the instrument was requested to point. The
     * DEC_PNT keyword should be used to give the actual pointed direction.
     */
    DEC_NOM(HduType.ANY, ValueType.REAL, "nominal declination of the observation"),

    /**
     * The value field shall contain a floating point number giving the declination of the observed object in units of
     * decimal degrees. The coordinate reference frame is given by the RADECSYS keyword, and the coordinate epoch is
     * given by the EQUINOX keyword.
     */
    DEC_OBJ(HduType.ANY, ValueType.REAL, "declination of the observed object"),

    /**
     * The value field shall contain a floating point number giving the declination of the pointing direction in units
     * of decimal degrees. The coordinate reference frame is given by the RADECSYS keyword, and the coordinate epoch is
     * given by the EQUINOX keyword. The precise definition of this keyword is instrument-specific, but typically the
     * pointed direction corresponds to the optical axis of the instrument. This keyword gives a mean value in cases
     * where the pointing axis was not fixed during the entire observation.
     */
    DEC_PNT(HduType.ANY, ValueType.REAL, "declination of the pointed direction of the instrument"),

    /**
     * The value field shall contain a floating point number giving the declination of the space craft (or telescope
     * platform) X axis during the observation in decimal degrees. The coordinate reference frame is given by the
     * RADECSYS keyword, and the coordinate epoch is given by the EQUINOX keyword. This keyword gives a mean value in
     * cases where the axis was not fixed during the entire observation.
     */
    DEC_SCX(HduType.ANY, ValueType.REAL, "declination of the X spacecraft axis"),

    /**
     * The value field shall contain a floating point number giving the declination of the space craft (or telescope
     * platform) Z axis during the observation in decimal degrees. The coordinate reference frame is given by the
     * RADECSYS keyword, and the coordinate epoch is given by the EQUINOX keyword. This keyword gives a mean value in
     * cases where the axis was not fixed during the entire observation.
     */
    DEC_SCZ(HduType.ANY, ValueType.REAL, "declination of the Z spacecraft axis"),

    /**
     * The value field shall contain a floating point number giving the geographic latitude from which the observation
     * was made in units of degrees.
     */
    LATITUDE(HduType.ANY, ValueType.REAL, "geographic latitude of the observation"),

    /**
     * The value field shall contain a floating point number giving the angle between the direction of the observation
     * (e.g., the optical axis of the telescope or the position of the target) and the moon, measured in degrees.
     */
    MOONANGL(HduType.ANY, ValueType.REAL, "angle between the observation and the moon"),

    /**
     * The value field shall contain a character string giving a name for the observed object that conforms to the IAU
     * astronomical object naming conventions. The value of this keyword is more strictly constrained than for the
     * standard OBJECT keyword which in practice has often been used to record other ancillary information about the
     * observation (e.g. filter, exposure time, weather conditions, etc.).
     */
    OBJNAME(HduType.ANY, ValueType.STRING, "AU name of observed object"),

    /**
     * The value field shall contain a character string which uniquely identifies the dataset contained in the FITS
     * file. This is typically a sequence number that can contain a mixture of numerical and character values. Example:
     * '10315-01-01-30A'
     */
    OBS_ID(HduType.ANY, ValueType.STRING, "unique observation ID"),

    /**
     * The value field shall contain a floating point number giving the position angle of the y axis of the detector
     * projected on the sky, in degrees east of north. This keyword is synonymous with the CROTA2 WCS keyword.
     */
    ORIENTAT(HduType.IMAGE, ValueType.REAL, "position angle of image y axis (deg. E of N)"),

    /**
     * The value field shall contain a floating point number giving the position angle of the relevant aspect of
     * telescope pointing axis and/or instrument on the sky in units of degrees east of north. It commonly applies to
     * the orientation of a slit mask.
     */
    PA_PNT(HduType.ANY, ValueType.REAL, "position angle of the pointing"),

    /**
     * The value field gives the Right Ascension of the observation. It may be expressed either as a floating point
     * number in units of decimal degrees, or as a character string in 'HH:MM:SS.sss' format where the decimal point and
     * number of fractional digits are optional. The coordinate reference frame is given by the RADECSYS keyword, and
     * the coordinate epoch is given by the EQUINOX keyword. Example: 180.6904 or '12:02:45.7'.
     */
    RA(HduType.ANY, ValueType.STRING, "R.A. of the observation"),

    /**
     * The value field shall contain a floating point number giving the nominal Right Ascension of the pointing
     * direction in units of decimal degrees. The coordinate reference frame is given by the RADECSYS keyword, and the
     * coordinate epoch is given by the EQUINOX keyword. The precise definition of this keyword is instrument-specific,
     * but typically the nominal direction corresponds to the direction to which the instrument was requested to point.
     * The RA_PNT keyword should be used to give the actual pointed direction.
     */
    RA_NOM(HduType.ANY, ValueType.REAL, "nominal R.A. of the observation"),

    /**
     * The value field shall contain a floating point number giving the Right Ascension of the observed object in units
     * of decimal degrees. The coordinate reference frame is given by the RADECSYS keyword, and the coordinate epoch is
     * given by the EQUINOX keyword.
     */
    RA_OBJ(HduType.ANY, ValueType.REAL, "R.A. of the observed object"),

    /**
     * The value field shall contain a floating point number giving the Right Ascension of the pointing direction in
     * units of decimal degrees. The coordinate reference frame is given by the RADECSYS keyword, and the coordinate
     * epoch is given by the EQUINOX keyword. The precise definition of this keyword is instrument-specific, but
     * typically the pointed direction corresponds to the optical axis of the instrument. This keyword gives a mean
     * value in cases where the pointing axis was not fixed during the entire observation.
     */
    RA_PNT(HduType.ANY, ValueType.REAL, "R.A. of the pointed direction of the instrument"),

    /**
     * The value field shall contain a floating point number giving the Right Ascension of the space craft (or telescope
     * platform) X axis during the observation in decimal degrees. The coordinate reference frame is given by the
     * RADECSYS keyword, and the coordinate epoch is given by the EQUINOX keyword. This keyword gives a mean value in
     * cases where the axis was not fixed during the entire observation.
     */
    RA_SCX(HduType.ANY, ValueType.REAL, "R.A. of the X spacecraft axis"),

    /**
     * The value field shall contain a floating point number giving the Right Ascension of the space craft (or telescope
     * platform) Y axis during the observation in decimal degrees. The coordinate reference frame is given by the
     * RADECSYS keyword, and the coordinate epoch is given by the EQUINOX keyword. This keyword gives a mean value in
     * cases where the axis was not fixed during the entire observation.
     */
    RA_SCY(HduType.ANY, ValueType.REAL, "R.A. of the Y spacecraft axis"),

    /**
     * The value field shall contain a floating point number giving the Right Ascension of the space craft (or telescope
     * platform) Z axis during the observation in decimal degrees. The coordinate reference frame is given by the
     * RADECSYS keyword, and the coordinate epoch is given by the EQUINOX keyword. This keyword gives a mean value in
     * cases where the axis was not fixed during the entire observation.
     */
    RA_SCZ(HduType.ANY, ValueType.REAL, "R.A. of the Z spacecraft axis"),

    /**
     * The value field shall contain a floating point number giving the angle between the direction of the observation
     * (e.g., the optical axis of the telescope or the position of the target) and the sun, measured in degrees.
     */
    SUNANGLE(HduType.ANY, ValueType.REAL, "angle between the observation and the sun"),

    // FITS keywords that have been widely used within the astronomical community.
    // These are the Keywords that describe the instrument that took the data.

    /**
     * The value field shall contain a character string which gives the name of the instrumental aperture though which
     * the observation was made. This keyword is typically used in instruments which have a selection of apertures which
     * restrict the field of view of the detector.
     */
    APERTURE(HduType.ANY, ValueType.STRING, "name of field of view aperture"),

    /**
     * The value field shall contain a character string which identifies the configuration or mode of the pre-processing
     * software that operated on the raw instrumental data to generate the data that is recorded in the FITS file.
     * Example: some X-ray satellite data may be recorded in 'BRIGHT', 'FAINT', or 'FAST' data mode.
     */
    DATAMODE(HduType.ANY, ValueType.STRING, "pre-processor data mode"),

    /**
     * The value field shall contain a character string giving the name of the detector within the instrument that was
     * used to make the observation. Example: 'CCD1'
     */
    DETNAM(HduType.ANY, ValueType.STRING, "name of the detector used to make the observation"),

    /**
     * The value field shall contain a character string which gives the name of the filter that was used during the
     * observation to select or modify the radiation that was transmitted to the detector. More than 1 filter may be
     * listed by using the FILTERn indexed keyword. The value 'none' or 'NONE' indicates that no filter was used.
     */
    FILTER(HduType.ANY, ValueType.STRING, "name of filter used during the observation"),

    /**
     * The value field of this indexed keyword shall contain a character string which gives the name of one of multiple
     * filters that were used during the observation to select or modify the radiation that was transmitted to the
     * detector. The value 'none' or 'NONE' indicates that no filter was used.
     */
    FILTERn(HduType.ANY, ValueType.STRING, "name of filters used during the observation"),

    /**
     * The value field shall contain a character string which gives the name of the defraction grating that was used
     * during the observation. More than 1 grating may be listed by using the GRATINGn indexed keyword. The value 'none'
     * or 'NONE' indicates that no grating was used.
     */
    GRATING(HduType.ANY, ValueType.STRING, "name of the grating used during the observation."),

    /**
     * The value field of this indexed keyword shall contain a character string which gives the name of one of multiple
     * defraction gratings that were used during the observation. The value 'none' or 'NONE' indicates that no grating
     * was used.
     */
    GRATINGn(HduType.ANY, ValueType.STRING, "name of gratings used during the observation."),

    /**
     * The value field shall contain a character string which gives the observing mode of the observation. This is used
     * in cases where the instrument or detector can be configured to operate in different modes which significantly
     * affect the resulting data. Examples: 'SLEW', 'RASTER', or 'POINTING'
     */
    OBS_MODE(HduType.ANY, ValueType.STRING, "instrumental mode of the observation"),

    /**
     * The value field shall contain an integer giving the data value at which the detector becomes saturated. This
     * keyword value may differ from the maximum value implied by the BITPIX in that more bits may be allocated in the
     * FITS pixel values than the detector can accommodate.
     */
    SATURATE(HduType.ANY, ValueType.INTEGER, "Data value at which saturation occurs"),

    // FITS keywords that have been widely used within the astronomical community.
    // These are the Keywords that describe the observation.

    /**
     * The value field shall contain a character string that gives the date on which the observation ended. This keyword
     * has the same format, and is used in conjunction with, the standard DATA-OBS keyword that gives the starting date
     * of the observation. These 2 keywords may give either the calendar date using the 'yyyy-mm-dd' format, or may give
     * the full date and time using the 'yyyy-mm-ddThh:mm:ss.sss' format.
     */
    DATE_END("DATE-END", HduType.ANY, ValueType.STRING, "date of the end of observation"),

    /**
     * The value field shall contain a floating point number giving the difference between the stop and start times of
     * the observation in units of seconds. This keyword is synonymous with the TELAPSE keyword.
     */
    ELAPTIME(HduType.ANY, ValueType.REAL, "elapsed time of the observation"),

    /**
     * The value field shall contain a floating point number giving the exposure time of the observation in units of
     * seconds. The exact definition of 'exposure time' is mission dependent and may, for example, include corrections
     * for shutter open and close duration, detector dead time, vignetting, or other effects. This keyword is synonymous
     * with the EXPTIME keyword.
     */
    EXPOSURE(HduType.ANY, ValueType.REAL, "exposure time"),

    /**
     * The value field shall contain a floating point number giving the exposure time of the observation in units of
     * seconds. The exact definition of 'exposure time' is mission dependent and may, for example, include corrections
     * for shutter open and close duration, detector dead time, vignetting, or other effects. This keyword is synonymous
     * with the EXPOSURE keyword.
     */
    EXPTIME(HduType.ANY, ValueType.REAL, "exposure time"),

    /**
     * The value field shall contain a floating point number giving the total integrated exposure time in units of
     * seconds corrected for detector 'dead time' effects which reduce the net efficiency of the detector. The ratio of
     * LIVETIME/ONTIME gives the mean dead time correction during the observation, which lies in the range 0.0 to 1.0.
     */
    LIVETIME(HduType.ANY, ValueType.REAL, "exposure time after deadtime correction"),

    /**
     * The value field shall contain a floating point number giving the total integrated exposure time of the
     * observation in units of seconds. ONTIME may be less than TELAPSE if there were intevals during the observation in
     * which the target was not observed (e.g., the shutter was closed, or the detector power was turned off).
     */
    ONTIME(HduType.ANY, ValueType.REAL, "integration time during the observation"),

    /**
     * The value field shall contain a floating point number giving the difference between the stop and start times of
     * the observation in units of seconds. This keyword is synonymous with the ELAPTIME keyword.
     */
    TELAPSE(HduType.ANY, ValueType.REAL, "elapsed time of the observation"),

    /**
     * The value field shall contain a character string that gives the time at which the observation ended. This keyword
     * is used in conjunction with the DATE-END keyword to give the ending time of the observation; the DATE-END keyword
     * gives the ending calendar date, with format 'yyyy-mm-dd', and TIME-END gives the time within that day using the
     * format 'hh:mm:ss.sss...'. This keyword should not be used if the time is included directly as part of the
     * DATE-END keyword value with the format 'yyyy-mm-ddThh:mm:ss.sss'.
     */
    TIME_END("TIME-END", HduType.ANY, ValueType.STRING, "time at the end of the observation"),

    /**
     * The value field shall contain a character string that gives the time at which the observation started. This
     * keyword is used in conjunction with the standard DATE-OBS keyword to give the starting time of the observation;
     * the DATE-OBS keyword gives the starting calendar date, with format 'yyyy-mm-dd', and TIME-OBS gives the time
     * within that day using the format 'hh:mm:ss.sss...'. This keyword should not be used if the time is included
     * directly as part of the DATE-OBS keyword value with the format 'yyyy-mm-ddThh:mm:ss.sss'.
     */
    TIME_OBS("TIME-OBS", HduType.ANY, ValueType.STRING, "time at the start of the observation"),

    // Maxim DL.Extension keywords that may be added or read by MaxIm DL.
    // https://github.com/nom-tam-fits/nom-tam-fits/blob/master/src/main/java/nom/tam/fits/header/extra/MaxImDLExt.java

    /**
     * if present the image has a valid Bayer color pattern.
     */
    BAYERPAT(HduType.IMAGE, ValueType.REAL, "image Bayer color pattern"),

    /**
     * Boltwood Cloud Sensor ambient temperature in degrees C.
     */
    BOLTAMBT(HduType.IMAGE, ValueType.REAL, "ambient temperature in degrees C"),

    /**
     * Boltwood Cloud Sensor cloud condition.
     */
    BOLTCLOU(HduType.IMAGE, ValueType.REAL, "Boltwood Cloud Sensor cloud condition."),

    /**
     * Boltwood Cloud Sensor daylight level.
     */
    BOLTDAY(HduType.IMAGE, ValueType.REAL, "Boltwood Cloud Sensor daylight level."),

    /**
     * Boltwood Cloud Sensor dewpoint in degrees C.
     */
    BOLTDEW(HduType.IMAGE, ValueType.REAL, "Boltwood Cloud Sensor dewpoint in degrees C."),

    /**
     * Boltwood Cloud Sensor humidity in percent.
     */
    BOLTHUM(HduType.IMAGE, ValueType.REAL, "Boltwood Cloud Sensor humidity in percent."),

    /**
     * Boltwood Cloud Sensor rain condition.
     */
    BOLTRAIN(HduType.IMAGE, ValueType.REAL, "Boltwood Cloud Sensor rain condition."),

    /**
     * Boltwood Cloud Sensor sky minus ambient temperature in degrees C.
     */
    BOLTSKYT(HduType.IMAGE, ValueType.REAL, "Boltwood Cloud Sensor sky minus ambient temperature in degrees C."),

    /**
     * Boltwood Cloud Sensor wind speed in km/h.
     */
    BOLTWIND(HduType.IMAGE, ValueType.REAL, "Boltwood Cloud Sensor wind speed in km/h."),

    /**
     * indicates calibration state of the image; B indicates bias corrected, D indicates dark corrected, F indicates
     * flat corrected.
     */
    CALSTAT(HduType.IMAGE, ValueType.REAL, "calibration state of the image"),

    /**
     * type of color sensor Bayer array or zero for monochrome.
     */
    COLORTYP(HduType.IMAGE, ValueType.REAL, "type of color sensor"),

    /**
     * initial display screen stretch mode.
     */
    CSTRETCH(HduType.IMAGE, ValueType.REAL, "initial display screen stretch mode"),

    /**
     * Total dark time of the observation. This is the total time during which dark current is collected by the
     * detector. If the times in the extension are different the primary HDU gives one of the extension times.
     */
    DARKTIME(HduType.IMAGE, ValueType.REAL, "dark current integration time"),

    /**
     * Davis Instruments Weather Station ambient temperature in deg C
     */
    DAVAMBT(HduType.IMAGE, ValueType.REAL, "ambient temperature"),

    /**
     * Davis Instruments Weather Station barometric pressure in hPa
     */
    DAVBAROM(HduType.IMAGE, ValueType.REAL, "barometric pressure"),

    /**
     * Davis Instruments Weather Station dewpoint in deg C
     */
    DAVDEW(HduType.IMAGE, ValueType.REAL, "dewpoint in deg C"),

    /**
     * Davis Instruments Weather Station humidity in percent
     */
    DAVHUM(HduType.IMAGE, ValueType.REAL, "humidity in percent"),

    /**
     * Davis Instruments Weather Station solar radiation in W/m^2
     */
    DAVRAD(HduType.IMAGE, ValueType.REAL, "solar radiation"),

    /**
     * Davis Instruments Weather Station accumulated rainfall in mm/day
     */
    DAVRAIN(HduType.IMAGE, ValueType.REAL, "accumulated rainfall"),

    /**
     * Davis Instruments Weather Station wind speed in km/h
     */
    DAVWIND(HduType.IMAGE, ValueType.REAL, "wind speed"),

    /**
     * Davis Instruments Weather Station wind direction in deg
     */
    DAVWINDD(HduType.IMAGE, ValueType.REAL, "wind direction"),

    /**
     * status of pier flip for German Equatorial mounts.
     */
    FLIPSTAT(HduType.IMAGE, ValueType.REAL, "status of pier flip"),

    /**
     * Focuser position in steps, if focuser is connected.
     */
    FOCUSPOS(HduType.IMAGE, ValueType.REAL, "Focuser position in steps"),

    /**
     * Focuser step size in microns, if available.
     */
    FOCUSSZ(HduType.IMAGE, ValueType.REAL, "Focuser step size in microns"),

    /**
     * Focuser temperature readout in degrees C, if available.
     */
    FOCUSTEM(HduType.IMAGE, ValueType.REAL, "Focuser temperature readout"),

    /**
     * format of file from which image was read.
     */
    INPUTFMT(HduType.IMAGE, ValueType.REAL, "format of file"),

    /**
     * ISO camera setting, if camera uses ISO speeds.
     */
    ISOSPEED(HduType.IMAGE, ValueType.REAL, "ISO camera setting"),

    /**
     * records the geocentric Julian Day of the start of exposure.
     */
    JD(HduType.IMAGE, ValueType.REAL, "geocentric Julian Day"),

    /**
     * records the geocentric Julian Day of the start of exposure.
     */
    JD_GEO(HduType.IMAGE, ValueType.REAL, "geocentric Julian Da"),

    /**
     * records the Heliocentric Julian Date at the exposure midpoint.
     */
    JD_HELIO(HduType.IMAGE, ValueType.REAL, "Heliocentric Julian Date"),

    /**
     * records the Heliocentric Julian Date at the exposure midpoint.
     */
    JD_HELIO2("JD-HELIO", HduType.IMAGE, ValueType.REAL, "Heliocentric Julian Date"),

    /**
     * UT of midpoint of exposure.
     */
    MIDPOINT(HduType.IMAGE, ValueType.REAL, "midpoint of exposure"),

    /**
     * user-entered information; free-form notes.
     */
    NOTES(HduType.IMAGE, ValueType.REAL, "free-form note"),

    /**
     * nominal altitude of center of image
     */
    OBJCTALT(HduType.IMAGE, ValueType.REAL, "altitude of center of image"),

    /**
     * nominal azimuth of center of image
     */
    OBJCTAZ(HduType.IMAGE, ValueType.REAL, "nominal azimuth of center of image"),

    /**
     * nominal hour angle of center of image
     */
    OBJCTHA(HduType.IMAGE, ValueType.REAL, "nominal hour angle of center of image"),

    /**
     * indicates side-of-pier status when connected to a German Equatorial mount.
     */
    PIERSIDE(HduType.IMAGE, ValueType.REAL, "side-of-pier status"),

    /**
     * records the selected Readout Mode (if any) for the camera.
     */
    READOUTM(HduType.IMAGE, ValueType.REAL, "Readout Mode for the camera"),

    /**
     * Rotator angle in degrees, if focal plane rotator is connected.
     */
    ROTATANG(HduType.IMAGE, ValueType.REAL, "Rotator angle in degrees"),

    /**
     * indicates tile position within a mosaic.
     */
    TILEXY(HduType.IMAGE, ValueType.REAL, "tile position within a mosaic"),

    /**
     * X offset of Bayer array on imaging sensor.
     */
    XBAYROFF(HduType.IMAGE, ValueType.REAL, "X offset of Bayer array"),

    /**
     * Y offset of Bayer array on imaging sensor.
     */
    YBAYROFF(HduType.IMAGE, ValueType.REAL, "Y offset of Bayer array"),

    // NOAO. https://github.com/nom-tam-fits/nom-tam-fits/blob/master/src/main/java/nom/tam/fits/header/extra/NOAOExt.java

    ACTFREQ(HduType.PRIMARY, ValueType.NONE, ""),
    ACTHWV(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * Times for the active optics sensor measurements given as modified Julian dates.
     */
    ACTMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the active optics sensor measurements given as modified Julian dates.
     */
    ACTMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Active optics system position angle measurements in appropriate units.
     */
    ACTPAN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Active optics system position angle measurements in appropriate units.
     */
    ACTPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Active optics system linear position sensor measurements in appropriate units.
     */
    ACTPOS(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Active optics system linear position sensor measurements in appropriate units.
     */
    ACTPOSn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Active optics system pressure sensor measurements in appropriate units.
     */
    ACTPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Active optics system pressure sensor measurements in appropriate units.
     */
    ACTPREn(HduType.PRIMARY, ValueType.REAL, ""),

    ACTSTAT(HduType.PRIMARY, ValueType.NONE, ""),

    ACTSWV(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Active optics system temperature sensor measurements in degrees Celsius.
     */
    ACTTEM(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Active optics system temperature sensor measurements in degrees Celsius.
     */
    ACTTEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Active optics voltage sensor measurements in volts.
     */
    ACTVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Active optics voltage sensor measurements in volts.
     */
    ACTVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the adapter sensor measurements given as modified Julian dates.
     */
    ADAMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the adapter sensor measurements given as modified Julian dates.
     */
    ADAMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adapter position angle measurements in appropriate units.
     */
    ADAPAN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adapter position angle measurements in appropriate units.
     */
    ADAPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adapter linear position sensor measurements in appropriate units.
     */
    ADAPOS(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adapter linear position sensor measurements in appropriate units.
     */
    ADAPOSn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adapter pressure sensor measurements in appropriate units.
     */
    ADAPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adapter pressure sensor measurements in appropriate units.
     */
    ADAPREn(HduType.PRIMARY, ValueType.REAL, ""),

    ADAPSWV(HduType.PRIMARY, ValueType.NONE, ""),

    ADAPTER(HduType.PRIMARY, ValueType.NONE, ""),

    ADASTAT(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Adapter temperature sensor measurements in degrees Celsius.
     */
    ADATEM(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adapter temperature sensor measurements in degrees Celsius.
     */
    ADATEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adapter voltage sensor measurements in volts.
     */
    ADAVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adapter voltage sensor measurements in volts.
     */
    ADAVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Atmospheric dispersion compensator hardware identification.
     */
    ADC(HduType.PRIMARY, ValueType.STRING, "ADC Identification"),

    /**
     * Times for the ADC sensor measurements given as modified Julian dates.
     */
    ADCMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the ADC sensor measurements given as modified Julian dates.
     */
    ADCMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * ADC position angle measurements in appropriate units.
     */
    ADCPAN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * ADC position angle measurements in appropriate units.
     */
    ADCPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * ADC linear position sensor measurements in appropriate units.
     */
    ADCPOS(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * ADC linear position sensor measurements in appropriate units.
     */
    ADCPOSn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * ADC pressure sensor measurements in appropriate units.
     */
    ADCPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * ADC pressure sensor measurements in appropriate units.
     */
    ADCPREn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * ADC status.
     */
    ADCSTAT(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * Atmospheric dispersion compensator software identification.
     */
    ADCSWV(HduType.PRIMARY, ValueType.STRING, "ADC software version"),

    /**
     * ADC temperature sensor measurements in degrees Celsius.
     */
    ADCTEM(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * ADC temperature sensor measurements in degrees Celsius.
     */
    ADCTEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * ADC voltage sensor measurements in volts.
     */
    ADCVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * ADC voltage sensor measurements in volts.
     */
    ADCVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Declination of the aperture(s).
     */
    ADECnnn(HduType.PRIMARY, ValueType.STRING, "Aperture declination"),

    /**
     * Declination unit.
     */
    ADEUnnn(HduType.PRIMARY, ValueType.STRING, "Declination unit"),

    /**
     * Object declination for wavefront sensing.
     */
    ADODEC(HduType.PRIMARY, ValueType.STRING, "Adaptive optics object declination"),

    /**
     * Declination unit.
     */
    ADODECU(HduType.PRIMARY, ValueType.STRING, "Declination unit"),

    /**
     * Object coordinate epoch for wavefront sensing.
     */
    ADOEPOCH(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Object coordinate system equinox for wavefront sensing. A value before 1984 is Besselian otherwise it is Julian.
     */
    ADOEQUIN(HduType.PRIMARY, ValueType.REAL, ""),

    ADOFREQ(HduType.PRIMARY, ValueType.NONE, ""),

    ADOHWV(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Times for the adaptive optics sensor measurements given as modified Julian dates.
     */
    ADOMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the adaptive optics sensor measurements given as modified Julian dates.
     */
    ADOMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adaptive optics system position angle measurements in appropriate units.
     */
    ADOPAN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adaptive optics system position angle measurements in appropriate units.
     */
    ADOPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adaptive optics system linear position sensor measurements in appropriate units.
     */
    ADOPOS(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adaptive optics system linear position sensor measurements in appropriate units.
     */
    ADOPOSn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adaptive optics system pressure sensor measurements in appropriate units.
     */
    ADOPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adaptive optics system pressure sensor measurements in appropriate units.
     */
    ADOPREn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Object right ascension for wavefront sensing.
     */
    ADORA(HduType.PRIMARY, ValueType.STRING, "Adaptive optics object right ascension"),

    /**
     * Object coordinate system type for wavefront sensing.
     */
    ADORADEC(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * Right ascension unit.
     */
    ADORAU(HduType.PRIMARY, ValueType.STRING, "Right ascension unit"),

    ADOSTAT(HduType.PRIMARY, ValueType.NONE, ""),
    ADOSWV(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Adaptive optics system temperature sensor measurements in degrees Celsius.
     */
    ADOTEM(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adaptive optics system temperature sensor measurements in degrees Celsius.
     */
    ADOTEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Type of object used for wavefront sensing.
     */
    ADOTYPE(HduType.PRIMARY, ValueType.STRING, "Adaptive optics object type"),

    /**
     * Adaptive optics system voltage sensor measurements in volts.
     */
    ADOVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Adaptive optics system voltage sensor measurements in volts.
     */
    ADOVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Epoch of the coordinates for the aperture(s).
     */
    AEPOnnn(HduType.PRIMARY, ValueType.REAL, "Aperture coordinate epoch"),

    /**
     * Coordinate system equinox for the aperture(s). A value before 1984 is Besselian otherwise it is Julian.
     */
    AEQUnnn(HduType.PRIMARY, ValueType.REAL, "Aperture coordinate equinox"),

    /**
     * The computed airmass(es) at the time(s) given by the AMMJDn keywords.
     */
    AIRMASSn(HduType.PRIMARY, ValueType.REAL, "Airmass"),

    /**
     * Times for the airmass calculation given as modified Julian dates. The MJDHDR keyword may be used for the time at
     * which the image header is created or the MJD-OBS keyword may be used for the time of observation.
     */
    AMMJD(HduType.PRIMARY, ValueType.REAL, "MJD of airmass"),

    /**
     * Times for the airmass calculation given as modified Julian dates. The MJDHDR keyword may be used for the time at
     * which the image header is created or the MJD-OBS keyword may be used for the time of observation.
     */
    AMMJDn(HduType.PRIMARY, ValueType.REAL, "MJD of airmass"),

    /**
     * Amplifier integration or sample time.
     */
    AMPINTEG(HduType.PRIMARY_EXTENSION, ValueType.REAL, "Amplifier integration/sample time"),

    /**
     * Times for the amplifier sensor measurements given as modified Julian dates. The MJDHDR keyword may be used for
     * the time at which the image header is created or the MJD-OBS keyword may be used for the time of observation.
     */
    AMPMJD(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Times for the amplifier sensor measurements given as modified Julian dates. The MJDHDR keyword may be used for
     * the time at which the image header is created or the MJD-OBS keyword may be used for the time of observation.
     */
    AMPMJDn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Amplifier name.
     */
    AMPNAME(HduType.EXTENSION, ValueType.STRING, "Amplifier name"),

    /**
     * CCD amplifier position angle measurements in appropriate units.
     */
    AMPPAN(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD amplifier position angle measurements in appropriate units.
     */
    AMPPANn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD amplifier linear position sensor measurements in appropriate units.
     */
    AMPPOS(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD amplifier linear position sensor measurements in appropriate units.
     */
    AMPPOSn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD amplifier pressure sensor measurements in appropriate units.
     */
    AMPPRE(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD amplifier pressure sensor measurements in appropriate units.
     */
    AMPPREn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Amplifier unbinned pixel read time.
     */
    AMPREAD(HduType.PRIMARY_EXTENSION, ValueType.REAL, "Unbinned pixel read time"),

    /**
     * CCD amplifier sampling method used. This may also include any integration times.
     */
    AMPSAMPL(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Amplifier sampling method"),

    /**
     * Mapping of the CCD section to amplifier coordinates.
     */
    AMPSEC(HduType.EXTENSION, ValueType.STRING, "Amplifier section"),

    /**
     * The logical unbinned size of the amplifier readout in section notation. This includes drift scanning if
     * applicable.
     */
    AMPSIZE(HduType.EXTENSION, ValueType.STRING, "Amplifier readout size"),

    /**
     * CCD amplifier temperature sensor measurements in degrees Celsius.
     */
    AMPTEM(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD amplifier temperature sensor measurements in degrees Celsius.
     */
    AMPTEMn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD amplifier voltage sensor measurements in volts. }
     */
    AMPVOL(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD amplifier voltage sensor measurements in volts. }
     */
    AMPVOLn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Aperture position angle unit.
     */
    APAUnnn(HduType.PRIMARY, ValueType.STRING, "Aperture position angle unit"),

    /**
     * Declination of the aperture(s).
     */
    APDEC(HduType.PRIMARY, ValueType.STRING, "Aperture declination"),

    /**
     * Declination unit.
     */
    APDECU(HduType.PRIMARY, ValueType.STRING, "Declination unit"),

    /**
     * Aperture diameter of the aperture(s) for circular apertures and fibers. This is also used as an approximation to
     * the size of hexagonal lenses.
     */
    APDInnn(HduType.PRIMARY, ValueType.REAL, "Aperture diameter"),

    /**
     * Epoch of the coordinates for the aperture(s).
     */
    APEPOCH(HduType.PRIMARY, ValueType.REAL, "Aperture coordinate epoch"),

    /**
     * Coordinate system equinox for the aperture(s). A value before 1984 is Besselian otherwise it is Julian.
     */
    APEQUIN(HduType.PRIMARY, ValueType.REAL, "Aperture coordinate equinox"),

    /**
     * Aperture diameter of the aperture(s) for circular apertures and fibers. This is also used as an approximation to
     * the size of hexagonal lenses.
     */
    APERDIA(HduType.PRIMARY, ValueType.REAL, "Aperture diameter"),

    /**
     * Aperture length of the aperture(s) for slit apertures.
     */
    APERLEN(HduType.PRIMARY, ValueType.REAL, "Slit length"),

    /**
     * Aperture identification. This can be a physical aperture identification, the name of a mask, a fiber
     * configuration, etc. When there are many apertures the keyword APERTURE may be used to specify a configuration or
     * mask identification and the APER%4d keywords can be used to identify some information about the aperture such as
     * a fiber number.
     */
    APERnnn(HduType.PRIMARY, ValueType.STRING, "Aperture identification"),

    /**
     * Aperture position angle of the aperture(s) on the sky. This is measured using the longest dimension from north to
     * east for slits. For hexagonal lenslets it gives the position angle for one of the sides.
     */
    APERPA(HduType.PRIMARY, ValueType.REAL, "Aperture position angle"),

    /**
     * Aperture identification. This can be a physical aperture identification, the name of a mask, a fiber
     * configuration, etc. When there are many apertures the keyword APERTURE may be used to specify a configuration or
     * mask identification and the APER%4d keywords can be used to identify some information about the aperture such as
     * a fiber number.
     */
    APERWID(HduType.PRIMARY, ValueType.REAL, "Slit width"),

    /**
     * Aperture length of the aperture(s) for slit apertures.
     */
    APLEnnn(HduType.PRIMARY, ValueType.REAL, "Slit length"),

    /**
     * Aperture position angle of the aperture(s) on the sky. This is measured using the longest dimension from north to
     * east for slits. For hexagonal lenslets it gives the position angle for one of the sides.
     */
    APPAnnn(HduType.PRIMARY, ValueType.REAL, "Aperture position angle"),

    /**
     * Aperture position angle unit.
     */
    APPAUNIT(HduType.PRIMARY, ValueType.STRING, "Aperture position angle unit"),

    /**
     * Right ascension of the aperture(s).
     */
    APRA(HduType.PRIMARY, ValueType.STRING, "Aperture right ascension"),

    /**
     * Aperture coordinate system type for the aperture(s).
     */
    APRADEC(HduType.PRIMARY, ValueType.STRING, "Aperture coordinate system"),

    /**
     * Right ascension unit.
     */
    APRAU(HduType.PRIMARY, ValueType.STRING, "Right ascension unit"),

    /**
     * Aperture type. This is an from a dictionary. The common types are "slit", "hole", "fiber", "hexlens",
     * "hexlens+fiber" and "none". The last type is for aperture-less spectroscopy such as with objective prisms.
     * Typically for multiobject spectroscopy all the aperture types will be the same and the keyword will be APTYPE.
     */
    APTYnnn(HduType.PRIMARY, ValueType.STRING, "Aperture type"),

    /**
     * Aperture type. This is an from a dictionary. The common types are "slit", "hole", "fiber", "hexlens",
     * "hexlens+fiber" and "none". The last type is for aperture-less spectroscopy such as with objective prisms.
     * Typically for multiobject spectroscopy all the aperture types will be the same and the keyword will be APTYPE.
     */
    APTYPE(HduType.PRIMARY, ValueType.STRING, "Aperture type"),

    /**
     * Units of aperture dimensions. This applies to slit widths and lengths, fiber diameters, lenslet diameters, etc.
     * It may be a physical dimension or a projected angle on the sky.
     */
    APUNIT(HduType.PRIMARY, ValueType.STRING, "Aperture dimension unit"),

    /**
     * Units of aperture dimensions. This applies to slit widths and lengths, fiber diameters, lenslet diameters, etc.
     * It may be a physical dimension or a projected angle on the sky.
     */
    APUNnnn(HduType.PRIMARY, ValueType.STRING, "Aperture dimension unit"),

    /**
     * Aperture width of the aperture(s) for slit apertures.
     */
    APWInnn(HduType.PRIMARY, ValueType.REAL, "Slit width"),

    /**
     * Right ascension of the aperture(s).
     */
    ARAnnn(HduType.PRIMARY, ValueType.STRING, "Aperture right ascension"),

    /**
     * Right ascension unit.
     */
    ARAUnnn(HduType.PRIMARY, ValueType.STRING, "Right ascension unit"),

    /**
     * Archive hardware version.
     */
    ARCHHWV(HduType.PRIMARY, ValueType.STRING, "Archive hardware"),

    /**
     * Archive identification. This may be the same as the observation identification.
     */
    ARCHID(HduType.PRIMARY, ValueType.STRING, "Archive identification"),

    /**
     * The archive name in which the observation is archived.
     */
    ARCHIVE(HduType.PRIMARY, ValueType.STRING, "Archive"),

    /**
     * Archive status of data.
     */
    ARCHSTAT(HduType.PRIMARY, ValueType.STRING, "Archive status"),

    /**
     * Archive software version.
     */
    ARCHSWV(HduType.PRIMARY, ValueType.STRING, "Archive software version"),

    /**
     * Arcon predicted gain. This is the gain measured in the laboratory. The GAIN keyword may also have this value
     * initially but it is updated to the most recent estimate of the gain.
     */
    ARCONG(HduType.EXTENSION, ValueType.REAL, "Predicted gain"),

    /**
     * Arcon gain index ValueType.
     */
    ARCONGI(HduType.EXTENSION, ValueType.INTEGER, "Gain selection"),

    /**
     * Arcon predicted RMS readout noise. This is the value measured in the laboratory. The RDNOISE keyword may also
     * have this value initially but it is updated to the most current estimate.
     */
    ARCONRN(HduType.EXTENSION, ValueType.REAL, "Predicted readout noise"),

    /**
     * Arcon waveform complilation date.
     */
    ARCONWD(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Date CCD waveforms last compiled"),

    /**
     * Arcon waveform options enabled.
     */
    ARCONWM(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Arcon waveform options enabled"),

    /**
     * Aperture coordinate system type for the aperture(s).
     */
    ARDSnnn(HduType.PRIMARY, ValueType.STRING, "Aperture coordinate system"),

    /**
     * Transformation matrix between CCD and amplifier coordinates. Normally only two values will be non-zero and will
     * have values of 1 or -1. If missing the default is an identify matrix.
     */
    ATMn_n(HduType.EXTENSION, ValueType.REAL, "Amplifier transformation matrix"),

    /**
     * Transformation origin vector between CCD and amplifier coordinates.
     */
    ATVn(HduType.EXTENSION, ValueType.REAL, "Amplifier transformation vector"),

    /**
     * Section of the recorded image containing overscan or prescan data. This will be in binned pixels if binning is
     * done. Multiple regions may be recorded and specified, such as both prescan and overscan, but the first section
     * given by this parameter is likely to be the one used during calibration.
     */
    BIASnnn(HduType.EXTENSION, ValueType.STRING, "Bias section"),

    /**
     * Section of the recorded image containing overscan or prescan data. This will be in binned pixels if binning is
     * done. Multiple regions may be recorded and specified, such as both prescan and overscan, but the first section
     * given by this parameter is likely to be the one used during calibration.
     */
    BIASSEC(HduType.EXTENSION, ValueType.STRING, "Bias section"),

    /**
     * Description of bad pixels. The value is an IRAF bad pixel mask name.
     */
    BPM(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Bad pixels"),

    /**
     * Camera configuration.
     */
    CAMCONF(HduType.PRIMARY, ValueType.STRING, "Camera Configuration"),

    /**
     * Camera name.
     */
    CAMERA(HduType.PRIMARY, ValueType.STRING, "Camera name"),

    /**
     * Camera focus.
     */
    CAMFOCUS(HduType.PRIMARY, ValueType.REAL, "Camera focus"),

    /**
     * Camera hardware version.
     */
    CAMHWV(HduType.PRIMARY, ValueType.STRING, "Camera version"),

    /**
     * Times for the instrument sensor measurements given as modified Julian dates.
     */
    CAMMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the instrument sensor measurements given as modified Julian dates.
     */
    CAMMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Camera position angle measurements in appropriate units.
     */
    CAMPAN(HduType.PRIMARY, ValueType.REAL, "Camera position angle"),

    /**
     * Camera position angle measurements in appropriate units.
     */
    CAMPANn(HduType.PRIMARY, ValueType.REAL, "Camera position angle"),

    /**
     * Camera linear position sensor measurements in appropriate units.
     */
    CAMPOS(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Camera linear position sensor measurements in appropriate units.
     */
    CAMPOSn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Camera pressure sensor measurements in appropriate units.
     */
    CAMPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Camera pressure sensor measurements in appropriate units.
     */
    CAMPREn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Camera status.
     */
    CAMSTAT(HduType.PRIMARY, ValueType.STRING, "Camera status"),

    /**
     * Camera software version.
     */
    CAMSWV(HduType.PRIMARY, ValueType.STRING, "Camera software version"),

    /**
     * Camera temperature sensor measurements in degrees Celsius.
     */
    CAMTEM(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Camera temperature sensor measurements in degrees Celsius.
     */
    CAMTEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Camera voltage sensor measurements in volts.
     */
    CAMVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Camera voltage sensor measurements in volts.
     */
    CAMVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Declination of the CCD center.
     */
    CCDDEC(HduType.PRIMARY_EXTENSION, ValueType.STRING, "CCD declination"),

    /**
     * Declination unit.
     */
    CCDDECU(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Declination unit"),

    /**
     * Epoch of the CCD center coordinates.
     */
    CCDEPOCH(HduType.PRIMARY_EXTENSION, ValueType.REAL, "CCD coordinate epoch"),

    /**
     * CCD coordinate system equinox. A value before 1984 is Besselian otherwise it is Julian.
     */
    CCDEQUIN(HduType.PRIMARY_EXTENSION, ValueType.REAL, "CCD coordinate equinox"),

    /**
     * CCD hardware version
     */
    CCDHWV(HduType.PRIMARY_EXTENSION, ValueType.STRING, "CCD version"),

    /**
     * Times for the CCD sensor measurements given as modified Julian dates. The MJDHDR keyword may be used for the time
     * at which the image header is created or the MJD-OBS keyword may be used for the time of observation.
     */
    CCDMJD(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Times for the CCD sensor measurements given as modified Julian dates. The MJDHDR keyword may be used for the time
     * at which the image header is created or the MJD-OBS keyword may be used for the time of observation.
     */
    CCDMJDn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD identification.
     */
    CCDNAME(HduType.PRIMARY_EXTENSION, ValueType.STRING, "CCD identification"),

    /**
     * Number of amplifiers used to readout the CCD. This keyword may be absent if only one amplifier is used.
     */
    CCDNAMPS(HduType.PRIMARY_EXTENSION, ValueType.INTEGER, "Number of amplifiers used"),

    /**
     * CCD position angle measurements in appropriate units.
     */
    CCDPAN(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD position angle measurements in appropriate units.
     */
    CCDPANn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD linear position sensor measurements in appropriate units.
     */
    CCDPOS(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD linear position sensor measurements in appropriate units.
     */
    CCDPOSn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD pressure sensor measurements in appropriate units.
     */
    CCDPRE(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD pressure sensor measurements in appropriate units.
     */
    CCDPREn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * The actual format size of the CCD. This is the same as the CCDSIZE keyword except in the case of drift scanning.
     */
    CCDPSIZE(HduType.PRIMARY_EXTENSION, ValueType.STRING, "CCD size"),

    /**
     * Right ascension of the CCD center.
     */
    CCDRA(HduType.PRIMARY_EXTENSION, ValueType.STRING, "CCD right ascension"),

    /**
     * CCD coordinate system type.
     */
    CCDRADEC(HduType.PRIMARY_EXTENSION, ValueType.STRING, "CCD coordinate system"),

    /**
     * Right ascension unit.
     */
    CCDRAU(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Right ascension unit"),

    /**
     * The unbinned section of the logical CCD pixel raster covered by the amplifier readout in section notation. The
     * section must map directly to the specified data section through the binning and CCD to image coordiante
     * transformation. The image data section (DATASEC) is specified with the starting pixel less than the ending pixel.
     * Thus the order of this section may be flipped depending on the coordinate transformation (which depends on how
     * the CCD coordinate system is defined).
     */
    CCDSEC(HduType.EXTENSION, ValueType.STRING, "Region of CCD read"),

    /**
     * The logical unbinned size of the CCD in section notation. Normally this would be the physical size of the CCD
     * unless drift scanning is done. This is the full size even when subraster readouts are done.
     */
    CCDSIZE(HduType.PRIMARY_EXTENSION, ValueType.STRING, "CCD size"),

    /**
     * CCD on-chip summing given as two or four integer numbers. These define the summing of CCD pixels in the amplifier
     * readout order. The first two numbers give the number of pixels summed in the serial and parallel directions
     * respectively. If the first pixel read out consists of fewer unbinned pixels along either direction the next two
     * numbers give the number of pixels summed for the first serial and parallel pixels. From this it is implicit how
     * many pixels are summed for the last pixels given the size of the CCD section (CCDSEC). It is highly recommended
     * that controllers read out all pixels with the same summing in which case the size of the CCD section will be the
     * summing factors times the size of the data section.
     */
    CCDSUM(HduType.EXTENSION, ValueType.STRING, "CCD on-chip summing"),

    /**
     * CCD software version
     */
    CCDSWV(HduType.PRIMARY_EXTENSION, ValueType.STRING, "CCD software version"),

    /**
     * CCD temperature sensor measurements in degrees Celsius.
     */
    CCDTEM(HduType.PRIMARY_EXTENSION, ValueType.REAL, "CCD temperature"),

    /**
     * CCD temperature sensor measurements in degrees Celsius.
     */
    CCDTEMn(HduType.PRIMARY_EXTENSION, ValueType.REAL, "CCD temperature"),

    /**
     * CCD voltage sensor measurements in volts.
     */
    CCDVOL(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * CCD voltage sensor measurements in volts.
     */
    CCDVOLn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the dispersion and the other axes are
     * spatial. The matrix implies a dispersion axis in the image coordinates.
     */
    CD1_1(HduType.EXTENSION, ValueType.REAL, "Coordinate scale matrix"),

    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the dispersion and the other axes are
     * spatial. The matrix implies a dispersion axis in the image coordinates.
     */
    CD1_2(HduType.EXTENSION, ValueType.REAL, "Coordinate scale matrix"),

    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the dispersion and the other axes are
     * spatial. The matrix implies a dispersion axis in the image coordinates.
     */
    CD11nnn(HduType.EXTENSION, ValueType.REAL, "Coordinate scale matrix"),

    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the dispersion and the other axes are
     * spatial. The matrix implies a dispersion axis in the image coordinates.
     */
    CD12nnn(HduType.EXTENSION, ValueType.REAL, "Coordinate scale matrix"),

    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the dispersion and the other axes are
     * spatial. The matrix implies a dispersion axis in the image coordinates.
     */
    CD2_1(HduType.EXTENSION, ValueType.REAL, "Coordinate scale matrix"),

    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the dispersion and the other axes are
     * spatial. The matrix implies a dispersion axis in the image coordinates.
     */
    CD2_2(HduType.EXTENSION, ValueType.REAL, "Coordinate scale matrix"),

    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the dispersion and the other axes are
     * spatial. The matrix implies a dispersion axis in the image coordinates.
     */
    CD21nnn(HduType.EXTENSION, ValueType.REAL, "Coordinate scale matrix"),

    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the dispersion and the other axes are
     * spatial. The matrix implies a dispersion axis in the image coordinates.
     */
    CD22nnn(HduType.EXTENSION, ValueType.REAL, "Spec coord matrix"),

    /**
     * Coordinate scale matrix for image world coordinates. This describes the scales and rotations of the coordinate
     * axes.
     */
    CHPANGLE(HduType.PRIMARY, ValueType.NONE, ""),

    CHPDIST(HduType.PRIMARY, ValueType.NONE, ""),

    CHPFREQ(HduType.PRIMARY, ValueType.NONE, ""),

    CHPHWV(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Times for the chopping system sensor measurements given as modified Julian dates.
     */
    CHPMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the chopping system sensor measurements given as modified Julian dates.
     */
    CHPMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    CHPNCHOP(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Chopping system position angle measurements in appropriate units. Note that CHPANGLE should be used for the
     * chopping angle and these keywords are for other system position angle measurements.
     */
    CHPPAN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Chopping system position angle measurements in appropriate units. Note that CHPANGLE should be used for the
     * chopping angle and these keywords are for other system position angle measurements.
     */
    CHPPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Chopping system linear position sensor measurements in appropriate units.
     */
    CHPPOS(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Chopping system linear position sensor measurements in appropriate units.
     */
    CHPPOSn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Chopping system pressure sensor measurements in appropriate units.
     */
    CHPPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Chopping system pressure sensor measurements in appropriate units.
     */
    CHPPREn(HduType.PRIMARY, ValueType.REAL, ""),

    CHPSTAT(HduType.PRIMARY, ValueType.NONE, ""),

    CHPSWV(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Chopping system temperature sensor measurements in degrees Celsius.
     */
    CHPTEM(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Chopping system temperature sensor measurements in degrees Celsius.
     */
    CHPTEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Chopping system voltage sensor measurements in volts.
     */
    CHPVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Chopping system voltage sensor measurements in volts.
     */
    CHPVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Dispersion limit for the region occupied by the spectrum.
     */
    CMAX1(HduType.EXTENSION, ValueType.REAL, "Spectrum dispersion limit"),

    /**
     * Cross-dispersion limit for the region occupied by the spectrum.
     */
    CMAX2(HduType.EXTENSION, ValueType.REAL, "Spectrum cross-dispersion limit"),

    /**
     * Dispersion limit for the region occupied by the spectrum.
     */
    CMIN1(HduType.EXTENSION, ValueType.REAL, "Spectrum dispersion limit"),

    /**
     * Cross-dispersion limit for the region occupied by the spectrum.
     */
    CMIN2(HduType.EXTENSION, ValueType.REAL, "Spectrum cross-dispersion limit"),

    /**
     * Observer comments.
     */
    CMMTnnn(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * Dispersion limit for the region occupied by the spectrum.
     */
    CMN1nnn(HduType.EXTENSION, ValueType.REAL, "Spectrum dispersion limit"),

    /**
     * Cross-dispersion limit for the region occupied by the spectrum.
     */
    CMN2nnn(HduType.EXTENSION, ValueType.REAL, "Spectrum cross-dispersion limit"),

    /**
     * Dispersion limit for the region occupied by the spectrum.
     */
    CMX1nnn(HduType.EXTENSION, ValueType.REAL, "Spectrum dispersion limit"),

    /**
     * Cross-dispersion limit for the region occupied by the spectrum.
     */
    CMX2nnn(HduType.EXTENSION, ValueType.REAL, "Spectrum cross-dispersion limit"),

    /**
     * Controller hardware version.
     */
    CONHWV(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Controller hardware version"),

    /**
     * Controller status.
     */
    CONSTAT(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Controller status"),

    /**
     * Controller software version.
     */
    CONSWV(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Controller software version"),

    /**
     * Detector controller name.
     */
    CONTROLR(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Detector controller"),

    /**
     * Correctors in the optical path.
     */
    CORRCT(HduType.PRIMARY, ValueType.STRING, "Corrector"),

    /**
     * Correctors in the optical path.
     */
    CORRCTn(HduType.PRIMARY, ValueType.STRING, "Corrector"),

    /**
     * Correctors in the optical path.
     */
    CORRCTOR(HduType.PRIMARY, ValueType.STRING, "Corrector Identification"),

    /**
     * Default cross dispersion unit.
     */
    CROSUNIT(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Declination unit"),

    /**
     * Default cross dispersion coordinate ValueType.
     */
    CROSVAL(HduType.PRIMARY_EXTENSION, ValueType.REAL, "Cross dispersion coordinate"),

    /**
     * Reference spectrum pixel coordinate. Generally this should be the at the center of the spectrum. In raw data the
     * spectrum position(s) may be predicted apart from an offset that will be determined during data reduction.
     */
    CRP1nnn(HduType.EXTENSION, ValueType.REAL, "Coordinate reference pixel"),

    /**
     * Reference spectrum pixel coordinate. Generally this should be the at the center of the spectrum. In raw data the
     * spectrum position(s) may be predicted apart from an offset that will be determined during data reduction.
     */
    CRP2nnn(HduType.EXTENSION, ValueType.REAL, "Coordinate reference pixel"),

    /**
     * Reference spectrum pixel coordinate. Generally this should be the at the center of the spectrum. In raw data the
     * spectrum position(s) may be predicted apart from an offset that will be determined during data reduction.
     */
    CRPIX1(HduType.EXTENSION, ValueType.REAL, "Coordinate reference pixel"),

    /**
     * Reference spectrum pixel coordinate. Generally this should be the at the center of the spectrum. In raw data the
     * spectrum position(s) may be predicted apart from an offset that will be determined during data reduction.
     */
    CRPIX2(HduType.EXTENSION, ValueType.REAL, "Coordinate reference pixel"),

    /**
     * Spectrum reference dispersion coordinate corresponding to the spectrum reference pixel coordinate. Note that by
     * definition WCS axis 1 is always the dispersion axis. The mapping of this WCS axis to the dispersion direction in
     * the image is given by the coordinate transformation matrix keywords. In raw data the reference dispersion
     * coordinate may be approximately predicted. This will be refined during data reductions.
     */
    CRV1nnn(HduType.EXTENSION, ValueType.REAL, "Coordinate reference value"),

    /**
     * Spectrum reference dispersion coordinate corresponding to the spectrum reference pixel coordinate. Note that by
     * definition WCS axis 1 is always the dispersion axis. The mapping of this WCS axis to the dispersion direction in
     * the image is given by the coordinate transformation matrix keywords. In raw data the reference dispersion
     * coordinate may be approximately predicted. This will be refined during data reductions.
     */
    CRV2nnn(HduType.EXTENSION, ValueType.REAL, "Coordinate reference value"),

    /**
     * Spectrum reference dispersion coordinate corresponding to the spectrum reference pixel coordinate. Note that by
     * definition WCS axis 1 is always the dispersion axis. The mapping of this WCS axis to the dispersion direction in
     * the image is given by the coordinate transformation matrix keywords. In raw data the reference dispersion
     * coordinate may be approximately predicted. This will be refined during data reductions.
     */
    CRVAL1(HduType.EXTENSION, ValueType.REAL, "Spectrum dispersion center"),

    /**
     * Reference right ascension coordinate corresponding to the image reference pixel coordinate. Note that by
     * definition WCS axis 1 is always the right ascension axis. The mapping of this WCS axis to the right ascension
     * direction in the image is given by the coordinate transformation matrix keywords. In raw data the reference right
     * ascension coordinate may be only approximate. This will be refined during data reductions.
     */
    CRVAL2(HduType.EXTENSION, ValueType.REAL, "Spectrum cross-dispersion center"),

    /**
     * Reference declination coordinate corresponding to the image reference pixel coordinate. Note that by definition
     * WCS axis 1 is always the declination axis. The mapping of this WCS axis to the declination direction in the image
     * is given by the coordinate transformation matrix keywords. In raw data the reference right ascension coordinate
     * may be only approximate. This will be refined during data reductions.
     */
    CTY1nnn(HduType.EXTENSION, ValueType.STRING, "Spectrum coordinate type"),

    /**
     * Coordinate type for image world coordinates. The IRAF WCS standards are used (which is generally the FITS
     * standard).
     */
    CTY2nnn(HduType.EXTENSION, ValueType.STRING, "Spectrum coordinate type"),

    /**
     * Coordinate type for image world coordinates. The IRAF WCS standards are used (which is generally the FITS
     * standard).
     */
    CTYP2nnn(HduType.EXTENSION, ValueType.STRING, "Coordinate type"),

    /**
     * Spectrum dispersion coordinate type. These are the FITS defined types.
     */
    CTYPE1(HduType.EXTENSION, ValueType.STRING, "Spectrum coordinate type"),

    /**
     * Coordinate type for image world coordinates. The IRAF WCS standards are used (which is generally the FITS
     * standard).
     */
    CTYPE2(HduType.EXTENSION, ValueType.STRING, "Spectrum coordinate type"),

    /**
     * Coordinate type for image world coordinates. The IRAF WCS standards are used (which is generally the FITS
     * standard).
     */
    CUN1nnn(HduType.EXTENSION, ValueType.STRING, "Spectrum coordinate unit"),

    /**
     * Coordinate reference unit for direct imaging world coordinates.
     */
    CUN2nnn(HduType.EXTENSION, ValueType.STRING, "Spectrum coordinate unit"),

    /**
     * Coordinate reference unit for direct imaging world coordinates.
     */
    CUNIT1(HduType.EXTENSION, ValueType.STRING, "Spectrum coordinate unit"),

    /**
     * Coordinate reference unit for direct imaging world coordinates.
     */
    CUNIT2(HduType.EXTENSION, ValueType.STRING, "Coordinate reference unit"),

    /**
     * Mapping of the CCD section to image coordinates.
     */
    DATASEC(HduType.EXTENSION, ValueType.STRING, "Image data section"),

    /**
     * Date at the end of the exposure. The format follows the FITS standard.
     */
    DATEEND(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Date at end of exposure"),

    /**
     * Date header creation. The format follows the FITS 'date' standard.
     */
    DATEHDR(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Date of header creation"),

    /**
     * Default date for the observation. This keyword is generally not used and is DATE-OBS keyword for the start of the
     * exposure on the detector is used.
     */
    DATEOBS(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Date of observation"),

    /**
     * Projected position angle of the positive declination axis on the detector. The position angle is measured
     * clockwise from the image y axis.
     */
    DECPANGL(HduType.PRIMARY, ValueType.REAL, "Position angle of Dec axis"),

    /**
     * Default declination units.
     */
    DECUNIT(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Declination unit"),

    /**
     * Detector configuration.
     */
    DETCONF(HduType.PRIMARY, ValueType.STRING, "Detector Configuration"),

    /**
     * Declination of the detector center.
     */
    DETDEC(HduType.PRIMARY, ValueType.STRING, "Detector delination"),

    /**
     * Declination unit.
     */
    DETDECU(HduType.PRIMARY, ValueType.STRING, "Delination unit"),

    /**
     * Detector name.
     */
    DETECTOR(HduType.PRIMARY, ValueType.STRING, "Detector name"),

    /**
     * Epoch of the detector center coordinates.
     */
    DETEPOCH(HduType.PRIMARY, ValueType.REAL, "Detector coordinate epoch"),

    /**
     * Detector coordinate system equinox. A value before 1984 is Besselian otherwise it is Julian.
     */
    DETEQUIN(HduType.PRIMARY, ValueType.REAL, "Detector coordinate equinox"),

    /**
     * Detector hardware version.
     */
    DETHWV(HduType.PRIMARY, ValueType.STRING, "Detector version"),

    /**
     * Times for the detector sensor measurements given as modified Julian dates. The MJDHDR keyword may be used for the
     * time at which the image header is created or the MJD-OBS keyword may be used for the time of observation.
     */
    DETMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the detector sensor measurements given as modified Julian dates. The MJDHDR keyword may be used for the
     * time at which the image header is created or the MJD-OBS keyword may be used for the time of observation.
     */
    DETMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Detector position angle measurements in appropriate units.
     */
    DETPAN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Detector position angle measurements in appropriate units.
     */
    DETPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Detector linear position sensor measurements in appropriate units.
     */
    DETPOS(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Detector linear position sensor measurements in appropriate units.
     */
    DETPOSn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Detector pressure sensor measurements in appropriate units.
     */
    DETPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Detector pressure sensor measurements in appropriate units.
     */
    DETPREn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Right ascension of the detector center.
     */
    DETRA(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Detector right ascension"),

    /**
     * Detector coordinate system type.
     */
    DETRADEC(HduType.PRIMARY, ValueType.STRING, "Detector coordinate system"),

    /**
     * Right ascension unit.
     */
    DETRAU(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Right ascension unit"),

    /**
     * Mapping of the CCD section to detector coordinates.
     */
    DETSEC(HduType.EXTENSION, ValueType.STRING, "Detector data section"),

    /**
     * The logical unbinned size of the detector in section notation. This is the full pixel raster size including, if
     * applicable, drift scanning or a mosaic format. This is the full size even when subraster readouts are done.
     */
    DETSIZE(HduType.PRIMARY, ValueType.STRING, "Detector size"),

    /**
     * Detector status.
     */
    DETSTAT(HduType.PRIMARY, ValueType.STRING, "Detector status"),

    /**
     * Detector software version. This will not generally be used and the controller software version will apply.
     */
    DETSWV(HduType.PRIMARY, ValueType.STRING, "Detector software version"),

    /**
     * Detector temperature sensor measurements in degrees Celsius.
     */
    DETTEM(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Detector temperature sensor measurements in degrees Celsius.
     */
    DETTEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Detector voltage sensor measurements in volts.
     */
    DETVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Detector voltage sensor measurements in volts.
     */
    DETVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Dewar identification.
     */
    DEWAR(HduType.PRIMARY, ValueType.STRING, "Dewar"),

    /**
     * Dewar hardware version.
     */
    DEWHWV(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Dewar hardware"),

    /**
     * Times for the dewar sensor measurements given as modified Julian dates. The MJDHDR keyword may be used for the
     * time at which the image header is created or the MJD-OBS keyword may be used for the time of observation.
     */
    DEWMJD(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Times for the dewar sensor measurements given as modified Julian dates. The MJDHDR keyword may be used for the
     * time at which the image header is created or the MJD-OBS keyword may be used for the time of observation.
     */
    DEWMJDn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Dewar position angle measurements in appropriate units.
     */
    DEWPAN(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Dewar position angle measurements in appropriate units.
     */
    DEWPANn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Dewar linear position sensor measurements in appropriate units.
     */
    DEWPOS(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Dewar linear position sensor measurements in appropriate units.
     */
    DEWPOSn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Dewar pressure sensor measurements in appropriate units.
     */
    DEWPRE(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Dewar pressure sensor measurements in appropriate units.
     */
    DEWPREn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Dewar status.
     */
    DEWSTAT(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Dewar status"),

    /**
     * Dewar software version.
     */
    DEWSWV(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Dewar software version"),

    /**
     * Dewar temperature sensor measurements in degrees Celsius.
     */
    DEWTEM(HduType.PRIMARY_EXTENSION, ValueType.REAL, "Dewar temperature"),

    /**
     * Dewar temperature sensor measurements in degrees Celsius.
     */
    DEWTEMn(HduType.PRIMARY_EXTENSION, ValueType.REAL, "Dewar temperature"),

    /**
     * Dewar voltage sensor measurements in volts.
     */
    DEWVOL(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Dewar voltage sensor measurements in volts.
     */
    DEWVOLn(HduType.PRIMARY_EXTENSION, ValueType.REAL, ""),

    /**
     * Times for the disperser sensor measurements given as modified Julian dates.
     */
    DISMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the disperser sensor measurements given as modified Julian dates.
     */
    DISMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Disperser position angle measurements in appropriate units.
     */
    DISPAN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Disperser position angle measurements in appropriate units.
     */
    DISPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * The detector axis along which the dispersion is most closely aligned.
     */
    DISPAXIS(HduType.PRIMARY, ValueType.INTEGER, "Dispersion axis"),

    /**
     * Approximate central dispersion/pixel on the detector.
     */
    DISPDW(HduType.PRIMARY, ValueType.REAL, "Dispersion"),

    /**
     * Disperser identification names.
     */
    DISPER(HduType.PRIMARY, ValueType.STRING, "Disperser"),

    /**
     * Disperser identification names.
     */
    DISPERn(HduType.PRIMARY, ValueType.STRING, "Disperser"),

    /**
     * Disperser linear position sensor measurements in appropriate units.
     */
    DISPOS(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Disperser linear position sensor measurements in appropriate units.
     */
    DISPOSn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Disperser pressure sensor measurements in appropriate units.
     */
    DISPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Disperser pressure sensor measurements in appropriate units.
     */
    DISPREn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Default dispersion coordinate unit.
     */
    DISPUNIT(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Dispersion coordinate unit"),

    /**
     * Default dispersion coordinate ValueType.
     */
    DISPVAL(HduType.PRIMARY_EXTENSION, ValueType.REAL, "Dispersion coordinate"),

    /**
     * Approximate central dispersion coordinate on the detector.
     */
    DISPWC(HduType.PRIMARY, ValueType.REAL, "Central dispersion coordinate"),

    /**
     * Disperser temperature sensor measurements in degrees Celsius.
     */
    DISTEM(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Disperser temperature sensor measurements in degrees Celsius.
     */
    DISTEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Disperser voltage sensor measurements in volts.
     */
    DISVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Disperser voltage sensor measurements in volts.
     */
    DISVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Average wind direction measurements measured east of north over the sampling period inside the dome.
     */
    DMEDIR(HduType.PRIMARY, ValueType.REAL, "Average wind direction"),

    /**
     * Average wind direction measurements measured east of north over the sampling period inside the dome.
     */
    DMEDIRn(HduType.PRIMARY, ValueType.REAL, "Average wind direction"),

    /**
     * Maximum wind speed over the sampling period inside the dome.
     */
    DMEGUS(HduType.PRIMARY, ValueType.REAL, "Maximum dome wind speed"),

    /**
     * Maximum wind speed over the sampling period inside the dome.
     */
    DMEGUSn(HduType.PRIMARY, ValueType.REAL, "Maximum dome wind speed"),

    /**
     * Times for the dome environment measurements given as modified Julian. For the wind measurements this is the start
     * of the sampling period.
     */
    DMEMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the dome environment measurements given as modified Julian. For the wind measurements this is the start
     * of the sampling period.
     */
    DMEMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Wind sampling period for the wind measurements inside the dome. If no value is given then the measurements are
     * assumed to be 'instantaneous'.
     */
    DMEPER(HduType.PRIMARY, ValueType.REAL, "Dome wind sampling"),

    /**
     * Wind sampling period for the wind measurements inside the dome. If no value is given then the measurements are
     * assumed to be 'instantaneous'.
     */
    DMEPERn(HduType.PRIMARY, ValueType.REAL, "Dome wind sampling"),

    /**
     * Temperatures Celsius inside the dome.
     */
    DMETEM(HduType.PRIMARY, ValueType.REAL, "Dome temperature"),

    /**
     * Temperatures Celsius inside the dome.
     */
    DMETEMn(HduType.PRIMARY, ValueType.REAL, "Dome temperature"),

    /**
     * Average wind speeds over the sampling period inside the dome.
     */
    DMEWIN(HduType.PRIMARY, ValueType.REAL, "Average dome wind speed"),

    /**
     * Average wind speeds over the sampling period inside the dome.
     */
    DMEWINn(HduType.PRIMARY, ValueType.REAL, "Average dome wind speed"),

    /**
     * Times for the dome sensor measurements given as modified Julian dates.
     */
    DOMMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the dome sensor measurements given as modified Julian dates.
     */
    DOMMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Dome position angle sensor measurements. This should be in degrees east of north for the center of the dome slit.
     */
    DOMPAN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Dome position angle sensor measurements. This should be in degrees east of north for the center of the dome slit.
     */
    DOMPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Dome linear position sensor measurements in appropriate units.
     */
    DOMPOS(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Dome linear position sensor measurements in appropriate units.
     */
    DOMPOSn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Dome pressure sensor measurements in appropriate units.
     */
    DOMPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Dome pressure sensor measurements in appropriate units.
     */
    DOMPREn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Dome status.
     */
    DOMSTAT(HduType.PRIMARY, ValueType.STRING, "Dome status"),

    /**
     * Dome temperature sensor measurements in degrees Celsius.
     */
    DOMTEM(HduType.PRIMARY, ValueType.REAL, "Dome temperature"),

    /**
     * Dome temperature sensor measurements in degrees Celsius.
     */
    DOMTEMn(HduType.PRIMARY, ValueType.REAL, "Dome temperature"),

    /**
     * Dome voltage sensor measurements in volts.
     */
    DOMVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Dome voltage sensor measurements in volts.
     */
    DOMVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Transformation matrix between CCD and detector coordinates. If missing the default is an identify matrix.
     */
    DTMn_n(HduType.EXTENSION, ValueType.REAL, "Detector transformation matrix"),

    /**
     * Transformation origin vector between CCD and detector coordinates.
     */
    DTVn(HduType.EXTENSION, ValueType.REAL, "Detector transformation vector"),

    /**
     * Average wind direction measurements measured east of north over the sampling period outside the dome at times
     * given by ENVMJDn keywords.
     */
    ENVDIR(HduType.PRIMARY, ValueType.REAL, "Average wind direction"),

    /**
     * Average wind direction measurements measured east of north over the sampling period outside the dome at times
     * given by ENVMJDn keywords.
     */
    ENVDIRn(HduType.PRIMARY, ValueType.REAL, "Average wind direction"),

    /**
     * Maximum wind speed in km/s over the sampling period outside the dome at times given by ENVMJDn keywords.
     */
    ENVGUS(HduType.PRIMARY, ValueType.REAL, "Maximum gust speed"),

    /**
     * Maximum wind speed in km/s over the sampling period outside the dome at times given by ENVMJDn keywords.
     */
    ENVGUSn(HduType.PRIMARY, ValueType.REAL, "Maximum gust speed"),

    /**
     * Relative humidity measurements at times given by ENVMJDn keywords.
     */
    ENVHUM(HduType.PRIMARY, ValueType.REAL, "Relative humidity"),

    /**
     * Relative humidity measurements at times given by ENVMJDn keywords.
     */
    ENVHUMn(HduType.PRIMARY, ValueType.REAL, "Relative humidity"),

    /**
     * Times for the site environment measurements given as modified Julian. For the wind measurements this is the start
     * of the sampling period.
     */
    ENVMJD(HduType.PRIMARY, ValueType.REAL, "Environment measurement time"),

    /**
     * Times for the site environment measurements given as modified Julian. For the wind measurements this is the start
     * of the sampling period.
     */
    ENVMJDn(HduType.PRIMARY, ValueType.REAL, "Environment measurement time"),

    /**
     * Wind sampling period for the wind measurements outside the dome at times given by ENVMJDn keywords. If no value
     * is given then the measurements are assumed to be 'instantaneous'.
     */
    ENVPER(HduType.PRIMARY, ValueType.REAL, "Wind sampling period"),

    /**
     * Wind sampling period for the wind measurements outside the dome at times given by ENVMJDn keywords. If no value
     * is given then the measurements are assumed to be 'instantaneous'.
     */
    ENVPERn(HduType.PRIMARY, ValueType.REAL, "Wind sampling period"),

    /**
     * Atmospheric pressure measurements at times given by ENVMJDn keywords.
     */
    ENVPRE(HduType.PRIMARY, ValueType.REAL, "Air pressure"),

    /**
     * Atmospheric pressure measurements at times given by ENVMJDn keywords.
     */
    ENVPREn(HduType.PRIMARY, ValueType.REAL, "Air pressure"),

    /**
     * Temperatures outside the dome at times given by ENVMJDn keywords.
     */
    ENVTEM(HduType.PRIMARY, ValueType.REAL, "Site temperature"),

    /**
     * Temperatures outside the dome at times given by ENVMJDn keywords.
     */
    ENVTEMn(HduType.PRIMARY, ValueType.REAL, "Site temperature"),

    /**
     * Precipitable water vapor measurements at times given by ENVMJDn keywords.
     */
    ENVWAT(HduType.PRIMARY, ValueType.REAL, "Precipitable water vapor"),

    /**
     * Precipitable water vapor measurements at times given by ENVMJDn keywords.
     */
    ENVWATn(HduType.PRIMARY, ValueType.REAL, "Precipitable water vapor"),

    /**
     * Average wind speeds over the sampling period outside the dome at times given by ENVMJDn keywords.
     */
    ENVWIN(HduType.PRIMARY, ValueType.REAL, "Average wind speed"),

    /**
     * Average wind speeds over the sampling period outside the dome at times given by ENVMJDn keywords.
     */
    ENVWINn(HduType.PRIMARY, ValueType.REAL, "Average wind speed"),

    /**
     * Error information. The sequence numbers are used to order the information.
     */
    ERRORnnn(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * Requested exposure time of the observation.
     */
    EXPREQ(HduType.PRIMARY_EXTENSION, ValueType.REAL, "Requested exposure time"),

    /**
     * Fiber identification for the fiber(s). The string consists of a fiber number, an object type number (0=sky,
     * 1=object, etc.), the right ascension and declination, and the object name or title. This can replace OBJNAME,
     * APRA/OBJRA, and APDEC/OBJDEC.
     */
    FIBER(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * Fiber identification for the fiber(s). The string consists of a fiber number, an object type number (0=sky,
     * 1=object, etc.), the right ascension and declination, and the object name or title. This can replace OBJNAME,
     * APRA/OBJRA, and APDEC/OBJDEC.
     */
    FIBnnn(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * Filter position given as filter wheel number or other filter system position measurement.
     */
    FILPOS(HduType.PRIMARY, ValueType.REAL, "Filter system position"),

    /**
     * Filter position given as filter wheel number or other filter system position measurement.
     */
    FILPOSn(HduType.PRIMARY, ValueType.REAL, "Filter system position"),

    /**
     * Filter type. This is the technical specification or observatory identification name.
     */
    FILTYP(HduType.PRIMARY, ValueType.STRING, "Filter type"),

    /**
     * Filter type. This is the technical specification or observatory identification name.
     */
    FILTYPn(HduType.PRIMARY, ValueType.STRING, "Filter type"),

    /**
     * Number of focus exposures in a focus sequence.
     */
    FOCNEXPO(HduType.PRIMARY, ValueType.INTEGER, "Number of focus exposures"),

    /**
     * Pixel shift on the detector between exposures in a focus sequence.
     */
    FOCSHIFT(HduType.PRIMARY, ValueType.REAL, "Shift between focus exposures"),

    /**
     * Starting focus value in focus sequence.
     */
    FOCSTART(HduType.PRIMARY, ValueType.REAL, "Starting focus"),

    /**
     * Focus increment step in focus sequence.
     */
    FOCSTEP(HduType.PRIMARY, ValueType.REAL, "Focus step"),

    /**
     * Amplifier gain in electrons per analog unit. This is the most current estimate of the gain.
     */
    GAIN(HduType.EXTENSION, ValueType.REAL, "Amplifier gain"),

    /**
     * Guider TV name.
     */
    GTV(HduType.PRIMARY, ValueType.STRING, "Guider TV"),

    /**
     * Guider TV filter names. This name is the astronomical standard name if applicable; i.e. U, B, Gunn I, etc. The
     * filter type and filter device position are given by other keywords.
     */
    GTVFIL(HduType.PRIMARY, ValueType.STRING, "Filter name"),

    /**
     * Guider TV filter names. This name is the astronomical standard name if applicable; i.e. U, B, Gunn I, etc. The
     * filter type and filter device position are given by other keywords.
     */
    GTVFILn(HduType.PRIMARY, ValueType.STRING, "Filter name"),

    /**
     * Guider TV filter position given as filter wheel number or other filter system position measurement.
     */
    GTVFPO(HduType.PRIMARY, ValueType.REAL, "Filter system position"),

    /**
     * Guider TV filter position given as filter wheel number or other filter system position measurement.
     */
    GTVFPOn(HduType.PRIMARY, ValueType.REAL, "Filter system position"),

    /**
     * Guider TV filter type. This is the technical specification or observatory identification name.
     */
    GTVFTY(HduType.PRIMARY, ValueType.STRING, "Filter type"),

    /**
     * Guider TV filter type. This is the technical specification or observatory identification name.
     */
    GTVFTYn(HduType.PRIMARY, ValueType.STRING, "Filter type"),

    /**
     * Guider TV identification and hardware version.
     */
    GTVHWV(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * Times for the guider television sensor measurements given as modified Julian dates.
     */
    GTVMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the guider television sensor measurements given as modified Julian dates.
     */
    GTVMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider television position angle measurements in appropriate units.
     */
    GTVPAN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider television position angle measurements in appropriate units.
     */
    GTVPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider television linear position sensor measurements in appropriate units.
     */
    GTVPOS(HduType.PRIMARY, ValueType.REAL, "Television position ()"),

    /**
     * Guider television linear position sensor measurements in appropriate units.
     */
    GTVPOSn(HduType.PRIMARY, ValueType.REAL, "Television position ()"),

    /**
     * Guider television pressure sensor measurements in appropriate units.
     */
    GTVPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider television pressure sensor measurements in appropriate units.
     */
    GTVPREn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider TV status.
     */
    GTVSTAT(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * Guider TV software version.
     */
    GTVSWV(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Guider television temperature sensor measurements in degrees Celsius.
     */
    GTVTEM(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider television temperature sensor measurements in degrees Celsius.
     */
    GTVTEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider television voltage sensor measurements in volts.
     */
    GTVVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider television voltage sensor measurements in volts.
     */
    GTVVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guide object declination.
     */
    GUIDEC(HduType.PRIMARY, ValueType.STRING, "Guider declination"),

    /**
     * Declination unit.
     */
    GUIDECU(HduType.PRIMARY, ValueType.STRING, "Declination unit"),

    /**
     * Guider identification and hardware version.
     */
    GUIDEHWV(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Guider name. Two of the names are 'manual' and 'none' for manual guiding or no guider, respectively.
     */
    GUIDER(HduType.PRIMARY, ValueType.STRING, "Guider name"),

    /**
     * Guider software version.
     */
    GUIDESWV(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Epoch of the guide object coordinates.
     */
    GUIEPOCH(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Guide object coordinate system equinox. A value before 1984 is Besselian otherwise it is Julian.
     */
    GUIEQUIN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the guider sensor measurements given as modified Julian dates.
     */
    GUIMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the guider sensor measurements given as modified Julian dates.
     */
    GUIMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider position angle measurements in appropriate units.
     */
    GUIPAN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider position angle measurements in appropriate units.
     */
    GUIPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider linear position sensor measurements in appropriate units. This might be used for guide probe positions.
     */
    GUIPOS(HduType.PRIMARY, ValueType.REAL, "Guider position ()"),

    /**
     * Guider linear position sensor measurements in appropriate units. This might be used for guide probe positions.
     */
    GUIPOSn(HduType.PRIMARY, ValueType.REAL, "Guider position ()"),

    /**
     * Guider pressure sensor measurements in appropriate units.
     */
    GUIPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider pressure sensor measurements in appropriate units.
     */
    GUIPREn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guide object right ascension.
     */
    GUIRA(HduType.PRIMARY, ValueType.STRING, "Guider right ascension"),

    /**
     * Guide object coordinate system type.
     */
    GUIRADEC(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * Guider correction rate.
     */
    GUIRATE(HduType.PRIMARY, ValueType.REAL, "Guider rate"),

    /**
     * Right ascension unit.
     */
    GUIRAU(HduType.PRIMARY, ValueType.STRING, "Right ascension unit"),

    /**
     * Guider status.
     */
    GUISTAT(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * Guider temperature sensor measurements in degrees Celsius.
     */
    GUITEM(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider temperature sensor measurements in degrees Celsius.
     */
    GUITEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider voltage sensor measurements in volts.
     */
    GUIVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Guider voltage sensor measurements in volts.
     */
    GUIVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Hour angle at TELMJD.
     */
    HA(HduType.PRIMARY, ValueType.STRING, "Hour angle"),

    /**
     * Image creation system hardware version.
     */
    IMAGEHWV(HduType.PRIMARY, ValueType.STRING, "Image creation hardware version"),

    /**
     * The image identification when there are multiple images within an observation. For detectors with CCDs this would
     * be a unique number assigned to each amplifier in the detector.
     */
    IMAGEID(HduType.EXTENSION, ValueType.INTEGER, "Image identification"),

    /**
     * Image creation system software version.
     */
    IMAGESWV(HduType.PRIMARY, ValueType.STRING, "Image creation software version"),

    /**
     * Instrument focus.
     */
    INSFOCUS(HduType.PRIMARY, ValueType.REAL, "Instrument focus"),

    /**
     * Times for the instrument sensor measurements given as modified Julian dates.
     */
    INSMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the instrument sensor measurements given as modified Julian dates.
     */
    INSMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Instrument position angle measurements in appropriate units.
     */
    INSPAN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Instrument position angle measurements in appropriate units.
     */
    INSPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Instrument linear position sensor measurements in appropriate units.
     */
    INSPOS(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Instrument linear position sensor measurements in appropriate units.
     */
    INSPOSn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Instrument pressure sensor measurements in appropriate units.
     */
    INSPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Instrument pressure sensor measurements in appropriate units.
     */
    INSPREn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Instrument status.
     */
    INSSTAT(HduType.PRIMARY, ValueType.STRING, "Instrument status"),

    /**
     * Instrument configuration.
     */
    INSTCONF(HduType.PRIMARY, ValueType.STRING, "Instrument configuration"),

    /**
     * Instrument temperature sensor measurements in degrees Celsius.
     */
    INSTEM(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Instrument temperature sensor measurements in degrees Celsius.
     */
    INSTEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Instrument hardware version.
     */
    INSTHWV(HduType.PRIMARY, ValueType.STRING, "Instrument hardware version"),

    /**
     * Instrument software version. ------------------------------------------------------------------
     */
    INSTSWV(HduType.PRIMARY, ValueType.STRING, "Instrument software version"),

    /**
     * Instrument voltage sensor measurements in volts.
     */
    INSVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Instrument voltage sensor measurements in volts.
     */
    INSVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * The keyword dictionary defining the keywords. This dictionary should be archived with the data.
     */
    KWDICT(HduType.PRIMARY, ValueType.STRING, "Keyword dictionary"),

    /**
     * Calibration lamp name
     */
    LAMP(HduType.PRIMARY, ValueType.STRING, "Calibration lamp"),

    /**
     * Calibration lamp type.
     */
    LAMPTYPE(HduType.PRIMARY, ValueType.STRING, "Lamp type"),

    /**
     * Times for the lamp sensor measurements given as modified Julian dates. The MJDHDR keyword may be used for the
     * time at which the image header is created or the MJD-OBS keyword may be used for the time of observation.
     */
    LMPMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the lamp sensor measurements given as modified Julian dates. The MJDHDR keyword may be used for the
     * time at which the image header is created or the MJD-OBS keyword may be used for the time of observation.
     */
    LMPMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Calibration lamp position angle measurements in appropriate units.
     */
    LMPPAN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Calibration lamp position angle measurements in appropriate units.
     */
    LMPPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Calibration lamp linear position sensor measurements in appropriate units.
     */
    LMPPOS(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Calibration lamp linear position sensor measurements in appropriate units.
     */
    LMPPOSn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Calibration lamp pressure sensor measurements in appropriate units.
     */
    LMPPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Calibration lamp pressure sensor measurements in appropriate units.
     */
    LMPPREn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Calibration lamp temperature sensor measurements in degrees Celsius.
     */
    LMPTEM(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Calibration lamp temperature sensor measurements in degrees Celsius.
     */
    LMPTEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Calibration lamp voltage sensor measurements in volts.
     */
    LMPVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Calibration lamp voltage sensor measurements in volts.
     */
    LMPVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Local siderial time at the start of the exposure.
     */
    LST_OBS("LST-OBS", HduType.PRIMARY_EXTENSION, ValueType.STRING, "LST of exposure start"),

    /**
     * Local siderial time at the end of the exposure.
     */
    LSTEND(HduType.PRIMARY_EXTENSION, ValueType.STRING, "LST at end of exposure"),

    /**
     * Local siderial time of the header creation.
     */
    LSTHDR(HduType.PRIMARY_EXTENSION, ValueType.STRING, "LST of header creation"),

    /**
     * Default local siderial time for the observation. This keyword is generally not used and is LST-OBS keyword for
     * the start of the exposure on the detector is used.
     */
    LSTOBS(HduType.PRIMARY_EXTENSION, ValueType.STRING, "LST of observation"),

    /**
     * Transformation matrix between CCD and image coordinates. If missing the default is an identify matrix.
     */
    LTMn_n(HduType.EXTENSION, ValueType.REAL, "Image transformation matrix"),

    /**
     * Transformation origin vector between CCD and image coordinates.
     */
    LTVn(HduType.EXTENSION, ValueType.REAL, "Image transformation vector"),

    /**
     * The maximum number of scanned (unbinned) lines used to form an output line. This is used with drift scanning or a
     * scan table. For long drift scans this will be the number of lines in the CCD.
     */
    MAXNSCAN(HduType.EXTENSION, ValueType.INTEGER, "Maximum number of scanned lines"),

    /**
     * The minimum number of scanned (unbinned) lines used to form an output line. This is used with drift scanning or a
     * scan table. This will only differ from MAXNSCAN if the initial lines in the output image are from the initial
     * ramp-up.
     */
    MINNSCAN(HduType.EXTENSION, ValueType.INTEGER, "Minimum number of scanned lines"),

    /**
     * Modified Julian date when the image header was created by the software. The fractional part of the date is given
     * to better than a second of time. Many header keywords may be sampled or computed at this time and this keyword is
     * the default for these.
     */
    MJDHDR(HduType.PRIMARY_EXTENSION, ValueType.REAL, "MJD of header creation"),

    /**
     * Default modified Julian date for the observation. The fractional part of the date is given to better than a
     * second of time. This keyword is generally not used and is MJD-OBS keyword for the start of the exposure on the
     * detector is used.
     */
    MJDOBS(HduType.PRIMARY_EXTENSION, ValueType.REAL, "MJD of observation"),

    /**
     * The number of amplifiers in the detector. When there is only a single amplifier used it may be absent since the
     * default value is 1.
     */
    NAMPS(HduType.PRIMARY, ValueType.INTEGER, "Number of Amplifiers"),

    /**
     * The number of CCDs in the detector. This is used with mosaics of CCD detectors. For a single CCD it may be absent
     * since the default value is 1.
     */
    NCCDS(HduType.PRIMARY, ValueType.INTEGER, "Number of CCDs"),

    NODANGLE(HduType.PRIMARY, ValueType.NONE, ""),

    NODDIST(HduType.PRIMARY, ValueType.NONE, ""),

    NODFREQ(HduType.PRIMARY, ValueType.NONE, ""),

    NODHWV(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Times for the nodding system sensor measurements given as modified Julian dates.
     */
    NODMJD(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Times for the nodding system sensor measurements given as modified Julian dates.
     */
    NODMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    NODNCHOP(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Nodding position angle measurements in appropriate units. Note that NODANGLE should be used for the nodding angle
     * and these keywords are for other system position angle measurements.
     */
    NODPAN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Nodding position angle measurements in appropriate units. Note that NODANGLE should be used for the nodding angle
     * and these keywords are for other system position angle measurements.
     */
    NODPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Nodding system linear position sensor measurements in appropriate units.
     */
    NODPOS(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Nodding system linear position sensor measurements in appropriate units.
     */
    NODPOSn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Nodding system pressure sensor measurements in appropriate units.
     */
    NODPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Nodding system pressure sensor measurements in appropriate units.
     */
    NODPREn(HduType.PRIMARY, ValueType.REAL, ""),

    NODSTAT(HduType.PRIMARY, ValueType.NONE, ""),

    NODSWV(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Nodding system temperature sensor measurements in degrees Celsius.
     */
    NODTEM(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Nodding system temperature sensor measurements in degrees Celsius.
     */
    NODTEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Nodding system voltage sensor measurements in volts.
     */
    NODVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Nodding system voltage sensor measurements in volts.
     */
    NODVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Number of coadded subexposures. When charge shuffling this gives the number of charge shuffled exposures.
     */
    NSUBEXPS(HduType.PRIMARY_EXTENSION, ValueType.INTEGER, "Number of subexposures"),

    /**
     * Declination of the target astronomical object(s).
     */
    OBJDEC(HduType.PRIMARY, ValueType.STRING, "Declination of object"),

    /**
     * Declination unit.
     */
    OBJDECU(HduType.PRIMARY, ValueType.STRING, "Declination unit"),

    /**
     * Epoch of the target astronomical object coordinate(s). This is given in years.
     */
    OBJEPOCH(HduType.PRIMARY, ValueType.REAL, "Epoch of object coordinates"),

    /**
     * Coordinate system equinox for the target astronomical object(s). A value before 1984 is Besselian otherwise it is
     * Julian.
     */
    OBJEQUIN(HduType.PRIMARY, ValueType.REAL, "Object coordinate equinox"),

    /**
     * Standard reference or catalog name for the target astronomical object(s). The name should follow IAU standards.
     * These keywords differ from the OBJECT keyword which is used to identify the observation.
     */
    OBJnnn(HduType.PRIMARY, ValueType.STRING, "Target object"),

    /**
     * Right ascension of the target astronomical object(s).
     */
    OBJRA(HduType.PRIMARY, ValueType.STRING, "Right ascension of object"),

    /**
     * Coordinate system type for the target astronomical object(s).
     */
    OBJRADEC(HduType.PRIMARY, ValueType.STRING, "Object coordinate system"),

    /**
     * Right ascension unit.
     */
    OBJRAU(HduType.PRIMARY, ValueType.STRING, "Right ascension unit"),

    /**
     * Type of target astronomical object(s). This is taken from a dictionary of names yet to be defined. Some common
     * types are 'galaxy', 'star', and 'sky'. If not particular object is targeted the type 'field' may be used.
     */
    OBJTnnn(HduType.PRIMARY, ValueType.STRING, "Type of object"),

    /**
     * Type of target astronomical object(s). This is taken from a dictionary of names yet to be defined. Some common
     * types are 'galaxy', 'star', and 'sky'. If not particular object is targeted the type 'field' may be used.
     */
    OBJTYPE(HduType.PRIMARY, ValueType.STRING, "Type of object"),

    /**
     * Declination of the observation. This may be distinct from the object coordinates and the telescope coordinates.
     * It may be used to indicate the requested observation coordinates.
     */
    OBSDEC(HduType.PRIMARY, ValueType.STRING, "Observation declination"),

    /**
     * Declination unit.
     */
    OBSDECU(HduType.PRIMARY, ValueType.STRING, "Declination unit"),

    /**
     * Epoch of the coordinates used in observation coordinates.
     */
    OBSEPOCH(HduType.PRIMARY, ValueType.REAL, "Observation coordinate epoch"),

    /**
     * Equinox of coordinates used in observation coordinates. A value before 1984 is Besselian otherwise it is Julian.
     */
    OBSEQUIN(HduType.PRIMARY, ValueType.REAL, "Observation coordinate equinox"),

    /**
     * Observatory identification for the site of the observation.
     */
    OBSERVAT(HduType.PRIMARY, ValueType.STRING, "Observatory"),

    /**
     * The unique observatory observation identification. This serves to identify all data from the same observation.
     */
    OBSID(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Observation identification"),

    /**
     * Right ascension of the observation. This may be distinct from the object coordinates and the telescope
     * coordinates. It may be used to indicate the requested observation coordinates.
     */
    OBSRA(HduType.PRIMARY, ValueType.STRING, "Observation right ascension"),

    /**
     * Coordinate system used in observation coordinates.
     */
    OBSRADEC(HduType.PRIMARY, ValueType.STRING, "Observation coordinate system"),

    /**
     * Right ascension unit.
     */
    OBSRAU(HduType.PRIMARY, ValueType.STRING, "Right ascension unit"),

    /**
     * Name(s) of the observers.
     */
    OBSRVRnn(HduType.PRIMARY, ValueType.STRING, "Observer(s)"),

    /**
     * Status of the observation. -----------------------------------------------------------------
     */
    OBSSTAT(HduType.PRIMARY, ValueType.STRING, "Observation status"),

    /**
     * The type of observation such as an astronomical exposure or a particular type of calibration exposure.
     */
    OBSTYPE(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Observation type"),

    /**
     * Declination of the target astronomical object(s).
     */
    ODECnnn(HduType.PRIMARY, ValueType.STRING, "Declination of object"),

    /**
     * Declination unit.
     */
    ODEUnnn(HduType.PRIMARY, ValueType.STRING, "Declination unit"),

    /**
     * Epoch of the target astronomical object coordinate(s). This is given in years.
     */
    OEPOnnn(HduType.PRIMARY, ValueType.REAL, "Epoch of object coordinates"),

    /**
     * Coordinate system equinox for the target astronomical object(s). A value before 1984 is Besselian otherwise it is
     * Julian.
     */
    OEQUnnn(HduType.PRIMARY, ValueType.REAL, "Object coordinate equinox"),

    /**
     * Right ascension of the target astronomical object(s).
     */
    ORAnnn(HduType.PRIMARY, ValueType.STRING, "Right ascension of object"),

    /**
     * Right ascension unit.
     */
    ORAUnnn(HduType.PRIMARY, ValueType.STRING, "Right ascension unit"),

    /**
     * Coordinate system type for the target astronomical object(s).
     */
    ORDSnnn(HduType.PRIMARY, ValueType.STRING, "Object coordinate system"),

    /**
     * Status of calibration to data proportional to photons. For CCD data this means bias section correction, zero
     * level calibration, dark count calibration, and flat field calibration.
     */
    PHOTCAL(HduType.PRIMARY_EXTENSION, ValueType.LOGICAL, "Data proportional to photons?"),

    /**
     * Photometric conditions during the observation.
     */
    PHOTOMET(HduType.PRIMARY, ValueType.STRING, "Photometric conditions"),

    /**
     * Processing hardware used.
     */
    PIPEHW(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Processing hardware"),

    /**
     * Processing hardware used.
     */
    PIPEHWn(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Processing hardware"),

    /**
     * Name of processing pipeline applied.
     */
    PIPELINE(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Pipeline used"),

    /**
     * Processing software version.
     */
    PIPESW(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Processing software"),

    /**
     * Processing software version.
     */
    PIPESWn(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Processing software"),

    /**
     * Projected pixel scale along axis n.
     */
    PIXSCALn(HduType.PRIMARY, ValueType.REAL, "Pixel scale"),

    /**
     * Unbinned pixel size along each dimension given in appropriate units. The units should be indicated in the
     * comment. The projected pixel size in arc seconds or wavelength are given by other parameters.
     */
    PIXSIZEn(HduType.PRIMARY_EXTENSION, ValueType.REAL, "Pixel size"),

    /**
     * Pixel limit for region occupied by the spectrum.
     */
    PMAX1(HduType.EXTENSION, ValueType.REAL, "Spectrum pixel limit"),

    /**
     * Pixel limit for region occupied by the spectrum.
     */
    PMAX2(HduType.EXTENSION, ValueType.REAL, "Spectrum pixel limit"),

    /**
     * Pixel limit for region occupied by the spectrum.
     */
    PMIN1(HduType.EXTENSION, ValueType.REAL, "Spectrum pixel limit"),

    /**
     * Pixel limit for region occupied by the spectrum.
     */
    PMIN2(HduType.EXTENSION, ValueType.REAL, "Spectrum pixel limit"),

    /**
     * Pixel limit for region occupied by the spectrum.
     */
    PMN1nnn(HduType.EXTENSION, ValueType.REAL, "Spectrum pixel limit"),

    /**
     * Pixel limit for region occupied by the spectrum.
     */
    PMN2n(HduType.EXTENSION, ValueType.REAL, "Spectrum pixel limit"),

    /**
     * Pixel limit for region occupied by the spectrum.
     */
    PMX1n(HduType.EXTENSION, ValueType.REAL, "Spectrum pixel limit"),

    /**
     * Pixel limit for region occupied by the spectrum.
     */
    PMX2n(HduType.EXTENSION, ValueType.REAL, "Spectrum pixel limit"),

    /**
     * CCD preflash time. If the times in the extension are different the primary HDU gives one of the extension times.
     */
    PREFLASH(HduType.PRIMARY_EXTENSION, ValueType.REAL, "Preflash time"),

    /**
     * Processing log information formatted as FITS comments.
     */
    PROCnnn(HduType.PRIMARY_EXTENSION, ValueType.STRING, ""),

    /**
     * Processing status.
     */
    PROCSTAT(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Processing status"),

    /**
     * The unique observatory proposal identification.
     */
    PROPID(HduType.PRIMARY, ValueType.STRING, "Proposal identification"),

    /**
     * The name or title of the proposal.
     */
    PROPOSAL(HduType.PRIMARY, ValueType.STRING, "Proposal title"),

    /**
     * Name(s) of the proposers.
     */
    PROPOSER(HduType.PRIMARY, ValueType.STRING, "Proposer(s)"),

    /**
     * Name(s) of the proposers.
     */
    PROPSRnn(HduType.PRIMARY, ValueType.STRING, "Proposer(s)"),

    /**
     * Default coordinate system equinox. A value before 1984 is Besselian otherwise it is Julian. If absent the default
     * is J2000.
     */
    RADECEQ(HduType.PRIMARY_EXTENSION, ValueType.REAL, "Default coordinate equinox"),

    /**
     * Projected position angle of the positive right ascension axis on the detector. The position angle is measured
     * clockwise from the image y axis.
     */
    RAPANGL(HduType.PRIMARY, ValueType.REAL, "Position angle of RA axis"),

    /**
     * Default right ascension units.
     */
    RAUNIT(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Right ascension unit"),

    /**
     * CCD readout noise in rms electrons. This is the most current estimate.
     */
    RDNOISE(HduType.EXTENSION, ValueType.REAL, "Readout noise"),

    /**
     * Amplifier unbinned pixel read time.
     */
    READTIME(HduType.PRIMARY_EXTENSION, ValueType.REAL, "Unbinned pixel read time"),

    /**
     * Archive identification. This may be the same as the observation identification.
     */
    RECNO(HduType.PRIMARY, ValueType.STRING, "Archive identification"),

    /**
     * Seeing estimates specified as the stellar full-width at half-maximum in arc seconds. There may be more than one
     * estimate. The times of the estimates are given by the SEEMJDn keyword.
     */
    SEEING(HduType.PRIMARY, ValueType.REAL, "FWHM"),

    /**
     * Seeing estimates specified as the stellar full-width at half-maximum in arc seconds. There may be more than one
     * estimate. The times of the estimates are given by the SEEMJDn keyword.
     */
    SEEINGn(HduType.PRIMARY, ValueType.REAL, "FWHM"),

    /**
     * Times for the seeing estimates given as modified Julian dates.
     */
    SEEMJD(HduType.PRIMARY, ValueType.REAL, "MJD for seeing estimate"),

    /**
     * Times for the seeing estimates given as modified Julian dates.
     */
    SEEMJDn(HduType.PRIMARY, ValueType.REAL, "MJD for seeing estimate"),

    /**
     * Exposure time of the nth subexposure. If all subexposures are the same length then only the first keyword, SEXP,
     * is needed. For charge shuffling the subexposure time is the total time for each charge shuffled exposure. There
     * is no finer division of the exposure times. Comments would be used to describe the subexposures of each charge
     * shuffled subexposure.
     */
    SEXP(HduType.PRIMARY_EXTENSION, ValueType.REAL, "Subexposure time"),

    /**
     * Exposure time of the nth subexposure. If all subexposures are the same length then only the first keyword, SEXP,
     * is needed. For charge shuffling the subexposure time is the total time for each charge shuffled exposure. There
     * is no finer division of the exposure times. Comments would be used to describe the subexposures of each charge
     * shuffled subexposure.
     */
    SEXPnnn(HduType.PRIMARY_EXTENSION, ValueType.REAL, "Subexposure time"),

    /**
     * Time for the shutter to close fully.
     */
    SHUTCLOS(HduType.PRIMARY, ValueType.REAL, "Shutter close time"),

    /**
     * Shutter identification and hardware version.
     */
    SHUTHWV(HduType.PRIMARY, ValueType.STRING, "Shutter hardware version"),

    /**
     * Time for the shutter to open fully.
     */
    SHUTOPEN(HduType.PRIMARY, ValueType.REAL, "Shutter open time"),

    /**
     * Shutter status.
     */
    SHUTSTAT(HduType.PRIMARY, ValueType.STRING, "Shutter status"),

    /**
     * Shutter software version.
     */
    SHUTSWV(HduType.PRIMARY, ValueType.STRING, "Shutter software version"),

    /**
     * Slit or mask hole identification for the aperture(s). The string consists of a number, an object type number
     * (0=sky, 1=object, etc.), the right ascension and declination, and the object name or title. declination, and the
     * object name or title. This can replace OBJNAME, APRA/OBJRA, and APDEC/OBJDEC.
     */
    SLIT(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * Slit or mask hole identification for the aperture(s). The string consists of a number, an object type number
     * (0=sky, 1=object, etc.), the right ascension and declination, and the object name or title. declination, and the
     * object name or title. This can replace OBJNAME, APRA/OBJRA, and APDEC/OBJDEC.
     */
    SLITnnn(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * FWHM of the object spectrum profile on the detector. The width is in the units of the spatial world coordinate
     * system. This may be approximate. It is particularly useful for specifying the profile width of fiber fed spectra.
     */
    SPECFWHM(HduType.EXTENSION, ValueType.REAL, "FWHM of spectrum"),

    /**
     * UTC of the start of each subexposure.
     */
    SUT(HduType.PRIMARY_EXTENSION, ValueType.STRING, "UTC of subexposure start"),

    /**
     * UTC of the start of each subexposure.
     */
    SUTn(HduType.PRIMARY_EXTENSION, ValueType.STRING, "UTC of subexposure start"),

    /**
     * FWHM of the object spectrum profile on the detector. The width is in the units of the spatial world coordinate
     * system. This may be approximate. It is particularly useful for specifying the profile width of fiber fed spectra.
     */
    SWIDnnn(HduType.EXTENSION, ValueType.REAL, "FWHM of spectrum"),

    /**
     * Modified Julian date at the time of the altitude/azimuth keywords.
     */
    TELAAMJD(HduType.PRIMARY, ValueType.REAL, "MJD at for alt/az"),

    /**
     * Telescope pointing altitude at the time given by TELAAMJD.
     */
    TELALT(HduType.PRIMARY, ValueType.STRING, "Telescope altitude"),

    /**
     * Telescope pointing azimuth at the time given by TELAAMJD.
     */
    TELAZ(HduType.PRIMARY, ValueType.STRING, "Telescope azimuth"),

    /**
     * Telescope configuration. The configuration defines the mirrors, correctors, light paths, etc.
     */
    TELCONF(HduType.PRIMARY, ValueType.STRING, "Telescope configuration"),

    /**
     * Telescope pointing declination.
     */
    TELDEC(HduType.PRIMARY, ValueType.STRING, "Telescope declination"),

    /**
     * Declination unit.
     */
    TELDECU(HduType.PRIMARY, ValueType.STRING, "Declination unit"),

    /**
     * Telescope pointing coordinate epoch.
     */
    TELEPOCH(HduType.PRIMARY, ValueType.REAL, "Telescope coordinate epoch"),

    /**
     * Telescope pointing coordinate system equinox. A value before 1984 is Besselian otherwise it is Julian.
     */
    TELEQUIN(HduType.PRIMARY, ValueType.REAL, "Telescope coordinate equinox"),

    /**
     * Telescope focus value in available units.
     */
    TELFOCUS(HduType.PRIMARY, ValueType.REAL, "Telescope focus"),

    /**
     * Time of zenith distance and hour angle
     */
    TELMJD(HduType.PRIMARY, ValueType.REAL, "Time of zenith distance and hour angle"),

    /**
     * Times for the telescope sensor measurements given as modified Julian dates.
     */
    TELMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Telescope position angle measurements in appropriate units. This could include altitude and azimuth measurements.
     */
    TELPAN(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Telescope position angle measurements in appropriate units. This could include altitude and azimuth measurements.
     */
    TELPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Telescope linear position sensor measurements in appropriate units.
     */
    TELPOS(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Telescope linear position sensor measurements in appropriate units.
     */
    TELPOSn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Telescope pressure sensor measurements in appropriate units.
     */
    TELPRE(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Telescope pressure sensor measurements in appropriate units.
     */
    TELPREn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Telescope pointing right ascension.
     */
    TELRA(HduType.PRIMARY, ValueType.STRING, "Telescope right ascension"),

    /**
     * Telescope pointing coordinate system type.
     */
    TELRADEC(HduType.PRIMARY, ValueType.STRING, "Telescope coordinate system"),

    /**
     * Right ascension unit.
     */
    TELRAU(HduType.PRIMARY, ValueType.STRING, "Right ascension unit"),

    /**
     * Telescope status.
     */
    TELSTAT(HduType.PRIMARY, ValueType.STRING, "Telescope status"),

    /**
     * Telescope control system software version.
     */
    TELTCS(HduType.PRIMARY, ValueType.STRING, "Telescope control system"),

    /**
     * Telescope temperature sensor measurements in degrees Celsius. The comment string may be modified to indicate the
     * location of the measurement.
     */
    TELTEM(HduType.PRIMARY, ValueType.REAL, "Telescope temperature"),

    /**
     * Telescope temperature sensor measurements in degrees Celsius. The comment string may be modified to indicate the
     * location of the measurement.
     */
    TELTEMn(HduType.PRIMARY, ValueType.REAL, "Telescope temperature"),

    /**
     * Declination telescope tracking rate in arc seconds per second.
     */
    TELTKDEC(HduType.PRIMARY, ValueType.REAL, "Tracking rate from siderial"),

    /**
     * Right ascension telescope tracking rate from siderial in arc seconds per second.
     */
    TELTKRA(HduType.PRIMARY, ValueType.REAL, "Tracking rate from siderial"),

    /**
     * Telescope hardware version.
     */
    TELVER(HduType.PRIMARY, ValueType.STRING, "Telescope version"),

    /**
     * Telescope voltage sensor measurements in volts.
     */
    TELVOL(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Telescope voltage sensor measurements in volts.
     */
    TELVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Time of exposure end in the TSYSEND system.
     */
    TIMEEND(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Time of exposure end"),

    /**
     * Time of header creation.
     */
    TIMEHDR(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Time of header creation"),

    /**
     * Default time system. All times which do not have a "timesys" element associated with them in this dictionary
     * default to this keyword. .
     */
    TIMESYS(HduType.PRIMARY, ValueType.STRING, "Default time system"),

    /**
     * Section of the recorded image to be kept after calibration processing. This is generally the part of the data
     * section containing useful data. The section is in in binned pixels if binning is done.
     */
    TRIMSEC(HduType.EXTENSION, ValueType.STRING, "Section of useful data"),

    /**
     * Time system for the TIMEEND keyword.
     */
    TSYSEND(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Time system for TIMEEND"),

    /**
     * Time system for the header creation keywords.
     */
    TSYSHDR(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Time system for header creation"),

    /**
     * Time system for the TIME-OBS keyword.
     */
    TSYSOBS(HduType.PRIMARY_EXTENSION, ValueType.STRING, "Time system for TIME-OBS"),

    /**
     * TV name.
     */
    TV(HduType.PRIMARY, ValueType.STRING, "TV"),

    /**
     * TV filter names. This name is the astronomical standard name if applicable; i.e. U, B, Gunn I, etc. The filter
     * type and filter device position are given by other keywords.
     */
    TVFILTn(HduType.PRIMARY, ValueType.STRING, "Filter name"),

    /**
     * Television focus value in available units.
     */
    TVFOCn(HduType.PRIMARY, ValueType.REAL, "Television focus"),

    /**
     * TV filter position given as filter wheel number or other filter system position measurement.
     */
    TVFPOSn(HduType.PRIMARY, ValueType.REAL, "Filter system position"),

    /**
     * TV filter type. This is the technical specification or observatory identification name.
     */
    TVFTYPn(HduType.PRIMARY, ValueType.STRING, "Filter type"),

    /**
     * TV identification and hardware version.
     */
    TVHWV(HduType.PRIMARY, ValueType.STRING, "TV Hardware"),

    /**
     * Times for the guider television sensor measurements given as modified Julian dates.
     */
    TVMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * TV name.
     */
    TVn(HduType.PRIMARY, ValueType.STRING, "TV"),

    /**
     * TV filter names. This name is the astronomical standard name if applicable; i.e. U, B, Gunn I, etc. The filter
     * type and filter device position are given by other keywords.
     */
    TVnFILTn(HduType.PRIMARY, ValueType.STRING, "Filter name"),

    /**
     * Television focus value in available units.
     */
    TVnFOCn(HduType.PRIMARY, ValueType.REAL, "Television focus"),

    /**
     * TV filter position given as filter wheel number or other filter system position measurement.
     */
    TVnFPOSn(HduType.PRIMARY, ValueType.REAL, "Filter system position"),

    /**
     * TV filter type. This is the technical specification or observatory identification name.
     */
    TVnFTYPn(HduType.PRIMARY, ValueType.STRING, "Filter type"),

    /**
     * TV identification and hardware version.
     */
    TVnHWV(HduType.PRIMARY, ValueType.STRING, "TV Hardware"),

    /**
     * Times for the guider television sensor measurements given as modified Julian dates.
     */
    TVnMJDn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Television position angle measurements in appropriate units.
     */
    TVnPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Television linear position sensor measurements in appropriate units.
     */
    TVnPOSn(HduType.PRIMARY, ValueType.REAL, "Television position ()"),

    /**
     * Television pressure sensor measurements in appropriate units.
     */
    TVnPREn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * TV status.
     */
    TVnSTAT(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * TV software version.
     */
    TVnSWV(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Television temperature sensor measurements in degrees Celsius.
     */
    TVnTEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Television voltage sensor measurements in volts.
     */
    TVnVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Television position angle measurements in appropriate units.
     */
    TVPANn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Television linear position sensor measurements in appropriate units.
     */
    TVPOSn(HduType.PRIMARY, ValueType.REAL, "Television position ()"),

    /**
     * Television pressure sensor measurements in appropriate units.
     */
    TVPREn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * TV status.
     */
    TVSTAT(HduType.PRIMARY, ValueType.STRING, ""),

    /**
     * TV software version.
     */
    TVSWV(HduType.PRIMARY, ValueType.NONE, ""),

    /**
     * Television temperature sensor measurements in degrees Celsius.
     */
    TVTEMn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Television voltage sensor measurements in volts.
     */
    TVVOLn(HduType.PRIMARY, ValueType.REAL, ""),

    /**
     * Altitude unit.
     */
    UNITALT(HduType.PRIMARY, ValueType.STRING, "Altitude unit"),

    /**
     * Plane angle unit.
     */
    UNITANG(HduType.PRIMARY, ValueType.STRING, "Plane angle unit"),

    /**
     * Focal plane aperture size unit.
     */
    UNITAP(HduType.PRIMARY, ValueType.STRING, "Aperture size unit"),

    /**
     * Area unit.
     */
    UNITAREA(HduType.PRIMARY, ValueType.STRING, "Area unit"),

    /**
     * Azimuth unit.
     */
    UNITAZ(HduType.PRIMARY, ValueType.STRING, "Azimuth unit"),

    /**
     * Capacitance unit.
     */
    UNITCAP(HduType.PRIMARY, ValueType.STRING, "Capacitance unit"),

    /**
     * Charge unit.
     */
    UNITCHAR(HduType.PRIMARY, ValueType.STRING, "Charge unit"),

    /**
     * Conductance unit.
     */
    UNITCOND(HduType.PRIMARY, ValueType.STRING, "Conductance unit"),

    /**
     * Current unit.
     */
    UNITCUR(HduType.PRIMARY, ValueType.STRING, "Current unit"),

    /**
     * Delination unit.
     */
    UNITDEC(HduType.PRIMARY, ValueType.STRING, "Declination unit"),

    /**
     * Energy unit.
     */
    UNITENER(HduType.PRIMARY, ValueType.STRING, "Energy unit"),

    /**
     * Event unit.
     */
    UNITEVNT(HduType.PRIMARY, ValueType.STRING, "Event unit"),

    /**
     * Flux unit.
     */
    UNITFLUX(HduType.PRIMARY, ValueType.STRING, "Flux unit"),

    /**
     * Force unit.
     */
    UNITFORC(HduType.PRIMARY, ValueType.STRING, "Force unit"),

    /**
     * Frequency unit.
     */
    UNITFREQ(HduType.PRIMARY, ValueType.STRING, "Frequency unit"),

    /**
     * Time of day unit.
     */
    UNITHOUR(HduType.PRIMARY, ValueType.STRING, "Time of day unit"),

    /**
     * Illuminance unit.
     */
    UNITILLU(HduType.PRIMARY, ValueType.STRING, "Illuminance unit"),

    /**
     * Inductance unit.
     */
    UNITINDU(HduType.PRIMARY, ValueType.STRING, "Inductance unit"),

    /**
     * Latitude unit.
     */
    UNITLAT(HduType.PRIMARY, ValueType.STRING, "Latitude unit"),

    /**
     * Length unit. A wavelength unit is also provided so this unit is primarily used to instrumental descriptions.
     */
    UNITLEN(HduType.PRIMARY, ValueType.STRING, "Length unit"),

    /**
     * Luminous flux unit.
     */
    UNITLFLX(HduType.PRIMARY, ValueType.STRING, "Luminous flux unit"),

    /**
     * Luminous intensity unit.
     */
    UNITLINT(HduType.PRIMARY, ValueType.STRING, "Luminous intensity unit"),

    /**
     * Longitude unit.
     */
    UNITLONG(HduType.PRIMARY, ValueType.STRING, "Longitude unit"),

    /**
     * Mass unit.
     */
    UNITMASS(HduType.PRIMARY, ValueType.STRING, "Mass unit"),

    /**
     * Magnetic density unit.
     */
    UNITMDEN(HduType.PRIMARY, ValueType.STRING, "Magnetic density unit"),

    /**
     * Magnetic field unit.
     */
    UNITMFLD(HduType.PRIMARY, ValueType.STRING, "Magnetic field unit"),

    /**
     * Magnetic flux unit.
     */
    UNITMFLX(HduType.PRIMARY, ValueType.STRING, "Magnetic flux unit"),

    /**
     * Position angle unit.
     */
    UNITPA(HduType.PRIMARY, ValueType.STRING, "Position angle unit"),

    /**
     * Power unit.
     */
    UNITPOW(HduType.PRIMARY, ValueType.STRING, "Wavelength unit"),

    /**
     * Pressure unit.
     */
    UNITPRES(HduType.PRIMARY, ValueType.STRING, "Pressure unit"),

    /**
     * Right ascension unit.
     */
    UNITRA(HduType.PRIMARY, ValueType.STRING, "Right ascension unit"),

    /**
     * Celestial rate of motion.
     */
    UNITRATE(HduType.PRIMARY, ValueType.STRING, "Celestial rate of motion"),

    /**
     * Resistance unit.
     */
    UNITRES(HduType.PRIMARY, ValueType.STRING, "Resistance unit"),

    /**
     * Solid angle unit.
     */
    UNITSANG(HduType.PRIMARY, ValueType.STRING, "Solid angle unit"),

    /**
     * Celestial separation unit.
     */
    UNITSEP(HduType.PRIMARY, ValueType.STRING, "Separation unit"),

    /**
     * Temperature unit.
     */
    UNITTEMP(HduType.PRIMARY, ValueType.STRING, "Temperature unit"),

    /**
     * Time unit.
     */
    UNITTIME(HduType.PRIMARY, ValueType.STRING, "Time unit"),

    /**
     * Velocity unit.
     */
    UNITVEL(HduType.PRIMARY, ValueType.STRING, "Velocity unit"),

    /**
     * Voltage unit.
     */
    UNITVOLT(HduType.PRIMARY, ValueType.STRING, "Voltage unit"),

    /**
     * UTC time at the start of the exposure.
     */
    UTC_OBS("UTC-OBS", HduType.PRIMARY_EXTENSION, ValueType.STRING, "UTC of exposure start"),

    /**
     * UTC at the end of the exposure.
     */
    UTCEND(HduType.PRIMARY_EXTENSION, ValueType.STRING, "UTC at end of exposure"),

    /**
     * UTC of header creation.
     */
    UTCHDR(HduType.PRIMARY_EXTENSION, ValueType.STRING, "UTC of header creation"),

    /**
     * Default UTC time for the observation. This keyword is generally not used and is UTC-OBS keyword for the start of
     * the exposure on the detector is used.
     */
    UTCOBS(HduType.PRIMARY_EXTENSION, ValueType.STRING, "UTC of observation"),

    /**
     * IRAF WCS attribute strings for all axes. These are defined by the IRAF WCS system.
     */
    WAT_nnn(HduType.PRIMARY_EXTENSION, ValueType.STRING, ""),

    /**
     * IRAF WCS attribute strings. These are defined by the IRAF WCS system.
     */
    WATn_nnn(HduType.PRIMARY_EXTENSION, ValueType.STRING, ""),

    /**
     * Descriptive string identifying the source of the astrometry used to derive the WCS. One example is the exposure
     * used to derive a WCS apart from the reference coordinate.
     */
    WCSAnnn(HduType.PRIMARY_EXTENSION, ValueType.STRING, "WCS Source"),

    /**
     * Descriptive string identifying the source of the astrometry used to derive the WCS. One example is the exposure
     * used to derive a WCS apart from the reference coordinate.
     */
    WCSASTRM(HduType.PRIMARY_EXTENSION, ValueType.STRING, "WCS Source"),

    /**
     * Dimensionality of the WCS physical system. In IRAF a WCS can have a higher dimensionality than the image.
     */
    WCSDIM(HduType.PRIMARY_EXTENSION, ValueType.INTEGER, "WCS dimensionality"),

    /**
     * Epoch of the coordinates used in the world coordinate system.
     */
    WCSEnnn(HduType.PRIMARY_EXTENSION, ValueType.REAL, "WCS coordinate epoch"),

    /**
     * Equinox when equatorial coordinates are used in the world coordinate system. A value before 1984 is Besselian
     * otherwise it is Julian.
     */
    WCSEPOCH(HduType.PRIMARY_EXTENSION, ValueType.REAL, "WCS coordinate epoch"),

    /**
     * Coordinate system type when equatorial coordinates are used in the world coordinate system.
     */
    WCSRADEC(HduType.PRIMARY_EXTENSION, ValueType.STRING, "WCS coordinate system"),

    /**
     * Coordinate system type when equatorial coordinates are used in the world coordinate system.
     */
    WCSRnnn(HduType.PRIMARY_EXTENSION, ValueType.STRING, "WCS coordinate system"),

    /**
     * Weather condition description. Generally this would be either 'clear' or 'partly cloudy'.
     */
    WEATHER(HduType.PRIMARY, ValueType.STRING, "Weather conditions"),

    /**
     * Zenith distance of telescope pointing at TELMJD.
     */
    ZD(HduType.PRIMARY, ValueType.REAL, "Zenith distance"),

    /**
     * Modified Julian date at the start of the exposure. The fractional part of the date is given to better than a
     * second of time.
     */
    MJD_OBS("MJD-OBS", HduType.PRIMARY_EXTENSION, ValueType.REAL, "MJD of exposure start"),

    // SBIG. https://github.com/nom-tam-fits/nom-tam-fits/blob/master/src/main/java/nom/tam/fits/header/extra/SBFitsExt.java

    /**
     * Aperture Area of the Telescope used in square millimeters. Note that we are specifying the area as well as the
     * diameter because we want to be able to correct for any central obstruction.
     */
    APTAREA(HduType.IMAGE, ValueType.REAL, "Aperture Area of the Telescope"),

    /**
     * Aperture Diameter of the Telescope used in millimeters.
     */
    APTDIA(HduType.IMAGE, ValueType.REAL, "Aperture Diameter of the Telescope"),

    /**
     * Upon initial display of this image use this ADU level for the Black level.
     */
    CBLACK(HduType.IMAGE, ValueType.INTEGER, "use this ADU level for the Black"),

    /**
     * Temperature of CCD when exposure taken.
     */
    CCD_TEMP("CCD-TEMP", HduType.IMAGE, ValueType.REAL, "Temperature of CCD"),

    /**
     * Altitude in degrees of the center of the image in degrees. Format is the same as the OBJCTDEC keyword.
     */
    CENTALT(HduType.IMAGE, ValueType.STRING, "Altitude of the center of the image"),

    /**
     * Azimuth in degrees of the center of the image in degrees. Format is the same as the OBJCTDEC keyword.
     */
    CENTAZ(HduType.IMAGE, ValueType.STRING, "Azimuth of the center of the image"),

    /**
     * Upon initial display of this image use this ADU level as the White level. For the SBIG method of displaying
     * images using Background and Range the following conversions would be used: Background = CBLACK Range = CWHITE -
     * CBLACK.
     */
    CWHITE(HduType.IMAGE, ValueType.INTEGER, "use this ADU level for the White"),

    /**
     * Electronic gain in e-/ADU.
     */
    EGAIN(HduType.IMAGE, ValueType.REAL, "Electronic gain in e-/ADU"),
    /*
     * Optional Keywords <p> The following Keywords are not defined in the FITS Standard but are defined in this
     * Standard. They may or may not be included by AIP Software Packages adhering to this Standard. Any of these
     * keywords read by an AIP Package must be preserved in files written. </p>
     */
    /**
     * Focal Length of the Telescope used in millimeters.
     */
    FOCALLEN(HduType.IMAGE, ValueType.REAL, "Focal Length of the Telescope"),

    /**
     * This indicates the type of image and should be one of the following: Light Frame Dark Frame Bias Frame Flat
     * Field.
     */
    IMAGETYP(HduType.IMAGE, ValueType.STRING, "type of image"),

    /**
     * This is the Declination of the center of the image in degrees. The format for this is ‘+25 12 34.111’ (SDD MM
     * SS.SSS) using a space as the separator. For the sign, North is + and South is -.
     */
    OBJCTDEC(HduType.IMAGE, ValueType.STRING, "Declination of the center of the image"),

    /**
     * This is the Right Ascension of the center of the image in hours, minutes and secon ds. The format for this is ’12
     * 24 23.123’ (HH MM SS.SSS) using a space as the separator.
     */
    OBJCTRA(HduType.IMAGE, ValueType.STRING, "Right Ascension of the center of the image"),

    /**
     * Add this ADU count to each pixel value to get to a zero - based ADU. For example in SBIG images we add 100 ADU to
     * each pixel to stop underflow at Zero ADU from noise. We would set PEDESTAL to - 100 in this case.
     */
    PEDESTAL(HduType.IMAGE, ValueType.INTEGER, "ADU count to each pixel value to get to a zero"),

    /**
     * This string indicates the version of this standard that the image was created to ie ‘SBFITSEXT Version 1.0’.
     */
    SBSTDVER(HduType.IMAGE, ValueType.STRING, "version of this standard"),

    /**
     * This is the setpoint of the cooling in degrees C. If it is not specified the setpoint is assumed to be the
     */
    SET_TEMP("SET-TEMP", HduType.IMAGE, ValueType.REAL, "setpoint of the cooling in degrees C"),

    /**
     * Latitude of the imaging location in degrees. Format is the same as the OBJCTDEC key word.
     */
    SITELAT(HduType.IMAGE, ValueType.STRING, "Latitude of the imaging location"),

    /**
     * Longitude of the imaging location in degrees. Format is the same as the OBJCTDEC keyword.
     */
    SITELONG(HduType.IMAGE, ValueType.STRING, "Longitude of the imaging location"),

    /**
     * Number of images combined to make this image as in Track and Accumulate or Co - Added images.
     */
    SNAPSHOT(HduType.IMAGE, ValueType.INTEGER, "Number of images combined"),

    /**
     * This indicates the name and version of the Software that initially created this file ie ‘SBIGs CCDOps Version
     * 5.10’.
     */
    SWCREATE(HduType.IMAGE, ValueType.STRING, "created version of the Software"),

    /**
     * This indicates the name and version of the Software that modified this file ie ‘SBIGs CCDOps Version 5.10’ and
     * the re can be multiple copies of this keyword. Only add this keyword if you actually modified the image and we
     * suggest placing this above the HISTORY keywords corresponding to the modifications made to the image.
     */
    SWMODIFY(HduType.IMAGE, ValueType.STRING, "modified version of the Software"),

    /**
     * If the image was auto-guided this is the exposure time in seconds of the tracker used to acquire this image. If
     * this keyword is not present then the image was unguided or hand guided.
     */
    TRAKTIME(HduType.IMAGE, ValueType.REAL, "exposure time in seconds of the tracker"),

    /**
     * Binning factor in width.
     */
    XBINNING(HduType.IMAGE, ValueType.INTEGER, "Binning factor in width"),

    /**
     * Sub frame X position of upper left pixel relative to whole frame in binned pixel units.
     */
    XORGSUBF(HduType.IMAGE, ValueType.INTEGER, "Sub frame X position"),

    /**
     * Pixel width in microns (after binning).
     */
    XPIXSZ(HduType.IMAGE, ValueType.REAL, "Pixel width in microns"),

    /**
     * Binning factor in height.
     */
    YBINNING(HduType.IMAGE, ValueType.INTEGER, "Binning factor in height"),

    /**
     * Sub frame Y position of upper left pixel relative to whole frame in binned pixel units.
     */
    YORGSUBF(HduType.IMAGE, ValueType.INTEGER, "Sub frame Y position"),

    /**
     * Pixel height in microns (after binning).
     */
    YPIXSZ(HduType.IMAGE, ValueType.REAL, "Pixel height in microns"),

    // Non-Standard. https://github.com/nom-tam-fits/nom-tam-fits/blob/master/src/main/java/nom/tam/fits/header/NonStandard.java

    /**
     * The HIERARCH keyword, when followed by spaces in columns 9 and 10 of the FITS card image, indicates that the ESO
     * HIERARCH keyword convention should be used to interpret the name and value of the keyword. The HIERARCH keyword
     * formally has no value because it is not followed by an equals sign in column 9. Under the HIERARCH convention the
     * actual name of the keyword begins in column 11 of the card image and is terminated by the equal sign ('=')
     * character. The name can contain any number of characters as long as it fits within columns 11 and 80 of the card
     * image and also leaves enough space for the equal sign separator and the value field. The name can contain any
     * printable ASCII text character, including spaces and lower-case characters, except for the equal sign character
     * which serves as the terminator of the name field. Leading and trailing spaces in the name field are not
     * significant, but embedded spaces within the name are significant. The value field follows the equals sign and
     * must conform to the syntax for a free-format value field as defined in the FITS Standard. The value field may be
     * null, in which case it contains only space characters, otherwise it may contain either a character string
     * enclosed in single quotes, the logical constant T or F, an integer number, a floating point number, a complex
     * integer number, or a complex floating point number. The value field may be followed by an optional comment
     * string. The comment field must be separated from the value field by a slash character ('/'). It is recommended
     * that the slash character be preceeded and followed by a space character. Example: "HIERARCH Filter Wheel = 12 /
     * filter position". In this example the logical name of the keyword is 'Filter Wheel' and the value is 12.
     */
    HIERARCH(HduType.ANY, ValueType.NONE, "denotes the HIERARCH keyword convention"),

    /**
     * The presence of this keyword with a value = T in an extension key indicates that the keywords contained in the
     * primary key (except the FITS Mandatory keywords, and any COMMENT, HISTORY or 'blank' keywords) are to be
     * inherited, or logically included in that extension key.
     */
    INHERIT(HduType.EXTENSION, ValueType.LOGICAL, "denotes the INHERIT keyword convention");

    private val header: FitsHeaderKey

    constructor(name: String, hduType: HduType, valueType: ValueType, comment: String) {
        header = FitsHeaderKeyItem(name, hduType, valueType, comment)
    }

    constructor(hduType: HduType, valueType: ValueType, comment: String) {
        header = FitsHeaderKeyItem(name, hduType, valueType, comment)
    }

    override val key
        get() = header.key

    override val comment
        get() = header.comment

    override val hduType
        get() = header.hduType

    override val valueType
        get() = header.valueType

    override fun n(vararg numbers: Int) = header.n(*numbers)

    companion object {

        @JvmStatic val NAXIS1 = NAXISn.n(1)
        @JvmStatic val NAXIS2 = NAXISn.n(2)
        @JvmStatic val NAXIS3 = NAXISn.n(3)

        @JvmStatic val CDELT1 = CDELTn.n(1)
        @JvmStatic val CDELT2 = CDELTn.n(2)

        @JvmStatic val CROTA1 = CROTAn.n(1)
        @JvmStatic val CROTA2 = CROTAn.n(2)
    }
}
