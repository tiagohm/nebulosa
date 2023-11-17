package nebulosa.fits

enum class Standard : FitsHeader {
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
     * Coordinate reference frame of major/minor axes. use RADESYS instead.
     */
    @Deprecated("use RADESYS instead.")
    RADECSYS(HduType.ANY, ValueType.STRING, "Coordinate reference frame of major/minor axes."),

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
    TIME_OBS("TIME-OBS", HduType.ANY, ValueType.STRING, "time at the start of the observation");

    private val header: FitsHeader

    constructor(name: String, hduType: HduType, valueType: ValueType, comment: String) {
        header = FitsHeaderImpl(name, hduType, valueType, comment)
    }

    constructor(hduType: HduType, valueType: ValueType, comment: String) {
        header = FitsHeaderImpl(name, hduType, valueType, comment)
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

        @JvmStatic val CTYPE1 = CTYPEn.n(1)
        @JvmStatic val CTYPE2 = CTYPEn.n(2)

        @JvmStatic val CRPIX1 = CRPIXn.n(1)
        @JvmStatic val CRPIX2 = CRPIXn.n(2)

        @JvmStatic val CRVAL1 = CRVALn.n(1)
        @JvmStatic val CRVAL2 = CRVALn.n(2)

        @JvmStatic val CDELT1 = CDELTn.n(1)
        @JvmStatic val CDELT2 = CDELTn.n(2)

        @JvmStatic val CROTA1 = CROTAn.n(1)
        @JvmStatic val CROTA2 = CROTAn.n(2)
    }
}
