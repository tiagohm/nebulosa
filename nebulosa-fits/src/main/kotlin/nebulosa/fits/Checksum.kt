package nebulosa.fits

enum class Checksum(hduType: HduType, valueType: ValueType, comment: String) : FitsHeader {
    /**
     * The value field of the CHECKSUM keyword shall contain a 16 character string, left justified starting in column
     * 12, containing the ASCII encoded complement of the checksum of the FITS HDU (Header and Data Unit). The algorithm
     * shall be the 32-bit 1's complement checksum and the ASCII encoding that are described in the checksum proposal.
     * The checksum is accumulated in FITS datastream order on the same HDU, identical in all respects, except that the
     * value of the CHECKSUM keyword shall be set to the string '0000000000000000' (ASCII 0's, hex 30) before the
     * checksum is computed.
     */
    CHECKSUM(HduType.ANY, ValueType.STRING, "checksum for the current HDU"),

    /**
     * The value field of the CHECKVER keyword shall contain a string, unique in the first 8 characters, which
     * distinguishes between any future alternative checksum algorithms which may be defined. The default value for a
     * missing keyword shall be 'COMPLEMENT' which will represent the algorithm defined in the current proposal. It is
     * recommended that this keyword be omitted from headers which implement the default ASCII encoded 32-bit 1's
     * complement algorithm.
     */
    CHECKVER(HduType.ANY, ValueType.STRING, "version of checksum algorithm"),

    /**
     * The value field of the DATASUM keyword shall be a character string containing the unsigned integer value of the
     * checksum of the data records of the HDU. For dataless HDU's, this keyword may either be omitted, or the value
     * field shall contain the string value '0', which is preferred. A missing DATASUM keyword asserts no knowledge of
     * the checksum of the data records.
     */
    DATASUM(HduType.ANY, ValueType.STRING, "checksum of the data records");

    private val header = FitsHeaderImpl(name, hduType, valueType, comment)

    override val key
        get() = header.key

    override val comment
        get() = header.comment

    override val hduType
        get() = header.hduType

    override val valueType
        get() = header.valueType

    override fun n(vararg numbers: Int) = header.n(*numbers)
}
