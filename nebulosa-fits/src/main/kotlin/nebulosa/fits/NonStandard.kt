package nebulosa.fits

enum class NonStandard(hduType: HduType, valueType: ValueType, comment: String?) : FitsHeader {
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

    private val header = FitsHeaderImpl(name, hduType, valueType, comment)

    override val key
        get() = header.key

    override val comment
        get() = header.comment

    override val hduType
        get() = header.hduType

    override val valueType
        get() = header.valueType

    override fun n(vararg numbers: Int) = this
}
