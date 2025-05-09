package nebulosa.fits

@Suppress("DEPRECATION")
enum class Compression(
    hduType: HduType, valueType: ValueType, comment: String,
    val keyword: FitsHeaderKey? = null,
) : FitsHeaderKey {
    /**
     * (required keyword) This keyword must have the logical value T. The value field of this keyword shall be ’T’ to
     * indicate that the FITS binary table extension contains a compressed BINTABLE, and that logically this extension
     * should be interpreted as a tile-compressed binary table.
     */
    ZTABLE(HduType.ANY, ValueType.LOGICAL, ""),

    /**
     * (required keyword) This keyword must have the logical value T. It indicates that the FITS binary table extension
     * contains a compressed image and that logically this extension should be interpreted as an image and not as a
     * table.
     */
    ZIMAGE(HduType.ANY, ValueType.LOGICAL, ""),

    /**
     * (required keyword) The value field of this keyword shall contain a character string giving the name of the
     * algorithm that must be used to decompress the image. Currently, values of GZIP 1 , GZIP 2 , RICE 1 , PLIO 1 , and
     * HCOMPRESS 1 are reserved, and the corresponding algorithms are described in a later section of this document .
     * The value RICE ONE is also reserved as an alias for RICE 1 .
     */
    ZCMPTYPE(HduType.ANY, ValueType.STRING, ""),

    /**
     * (required keyword) The value field of this keyword shall contain an integer that gives the value of the BITPIX
     * keyword in the uncompressed FITS image. 1
     */
    ZBITPIX(HduType.ANY, ValueType.INTEGER, "", FitsKeyword.BITPIX),

    /**
     * (required keyword) The value field of this keyword shall contain an integer that gives the value of the NAXIS
     * keyword in the uncompressed FITS image.
     */
    ZNAXIS(HduType.ANY, ValueType.INTEGER, "", FitsKeyword.NAXIS),

    /**
     * (required keywords) The value field of these keywords shall contain a positive integer that gives the value of
     * the NAXISn keywords in the uncompressed FITS image.
     */
    ZNAXISn(HduType.ANY, ValueType.INTEGER, "", FitsKeyword.NAXISn),

    /**
     * (optional keywords) The value of these indexed keywords (where n ranges from 1 to ZNAXIS ) shall contain a
     * positive integer representing the number o f pixels along axis n of the compression tiles. Each tile of pixels is
     * compressed separately and stored in a row of a variable-length vector column in the binary table. The size of
     * each image dimension (given by ZNAXISn ) is not required to be an integer multiple of ZTILEn, and if it is not,
     * then the last tile along that dimension of the image will contain fewer image pixels than the other tiles. If the
     * ZTILEn keywords are not present then the default ’row by row’ tiling will be assumed such that ZTILE1 = ZNAXIS1 ,
     * and the value of all the other ZTILEn keywords equals 1. The compressed image tiles are stored in the binary
     * table in t he same order that the first pixel in each tile appears in the FITS image; the tile containing the
     * first pixel in the image appears in the first row of the table, and the tile containing the last pixel in the
     * image appears in the last row of the binary table.
     */
    ZTILEn(HduType.ANY, ValueType.INTEGER, ""),

    /**
     * (optional keywords) These pairs of optional array keywords (where n is an integer index number starting with 1)
     * supply the name and value, respectively, of any algorithm-specific parameters that are needed to compress o r
     * uncompress the image. The value of ZVALn may have any valid FITS datatype. The order of the compression
     * parameters may be significant, and may be defined as part of the description of the specific decompression
     * algorithm.
     */
    ZNAMEn(HduType.ANY, ValueType.STRING, ""),

    /**
     * (optional keywords) These pairs of optional array keywords (where n is an integer index number starting with 1)
     * supply the name and value, respectively, of any algorithm-specific parameters that are needed to compress o r
     * uncompress the image. The value of ZVALn may have any valid FITS datatype. The order of the compression
     * parameters may be significant, and may be defined as part of the description of the specific decompression
     * algorithm.
     */
    ZVALn(HduType.ANY, ValueType.ANY, ""),

    /**
     * (optional keyword) Used to record the name of the image compression algorithm that was used to compress the
     * optional null pixel data mask. See the “Preserving undefined pixels with lossy compression” section for more
     * details.
     */
    ZMASKCMP(HduType.ANY, ValueType.STRING, ""),

    /**
     * The following optional keyword is defined to store a verbatim copy of the value and comment field of the
     * corresponding keyword in the original uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy of the original FITS file when the image is uncompressed.preserves the original SIMPLE keyword.may
     * only be used if the original uncompressed image was contained in the primary array of the FITS file.
     */
    ZSIMPLE(HduType.PRIMARY, ValueType.LOGICAL, "", FitsKeyword.SIMPLE),

    /**
     * The following optional keyword is defined to store a verbatim copy of the value and comment field of the
     * corresponding keyword in the original uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is uncompressed.preserves the original XTENSION
     * keyword.may only be used if the original uncompressed image was contained in in IMAGE extension.
     */

    ZTENSION(HduType.ANY, ValueType.STRING, "", FitsKeyword.XTENSION),

    /**
     * The following optional keyword is defined to store a verbatim copy of the value and comment field of the
     * corresponding keyword in the original uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy of the original FITS file when the image is uncompressed.preserves the original EXTEND keyword.may
     * only be used if the original uncompressed image was contained in the primary array of the FITS file.
     */
    ZEXTEND(HduType.PRIMARY, ValueType.LOGICAL, "", FitsKeyword.EXTEND),

    /**
     * The following optional keyword is defined to store a verbatim copy of the value and comment field of the
     * corresponding keyword in the original uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is uncompressed.preserves the original BLOCKED
     * keyword.may only be used if the original uncompressed image was contained in the primary array of the FITS file,
     */
    @Deprecated("no blocksize other that 2880 may be used")
    ZBLOCKED(HduType.PRIMARY, ValueType.LOGICAL, "", FitsKeyword.BLOCKED),

    /**
     * The following optional keyword is defined to store a verbatim copy of the value and comment field of the
     * corresponding keyword in the original uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is uncompressed.preserves the original PCOUNT
     * keyword.may only be used if the original uncompressed image was contained in in IMAGE extension.
     */
    ZPCOUNT(HduType.EXTENSION, ValueType.INTEGER, "", FitsKeyword.PCOUNT),

    /**
     * The following optional keyword is defined to store a verbatim copy of the value and comment field of the
     * corresponding keyword in the original uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is uncompressed.preserves the original GCOUNT
     * keyword.may only be used if the original uncompressed image was contained in in IMAGE extension.
     */
    ZGCOUNT(HduType.EXTENSION, ValueType.INTEGER, "", FitsKeyword.GCOUNT),

    /**
     * The following optional keyword is defined to store a verbatim copy of the value and comment field of the
     * corresponding keyword in the original uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is uncompressed.preserves the original CHECKSUM keyword.
     */
    ZHECKSUM(HduType.ANY, ValueType.STRING, "", Checksum.CHECKSUM),

    /**
     * The following optional keyword is defined to store a verbatim copy of the value and comment field of the
     * corresponding keyword in the original uncompressed FITS image. These keywords can be used to reconstruct an
     * identical copy o f the original FITS file when the image is uncompressed.preserves the original DATASUM
     */
    ZDATASUM(HduType.ANY, ValueType.STRING, "", Checksum.DATASUM),

    /**
     * (optional keyword) This keyword records the name of the algorithm that was used to quantize floating-point image
     * pixels into integer values which are then passed to the compression algorithm.
     */
    ZQUANTIZ(HduType.ANY, ValueType.STRING, ""),

    /**
     * (optional keyword) The value field of this keyword shall contain an integer that gives the seed value for the
     * random dithering pattern that was used when quantizing the floating-point pixel values. The value may range from
     * 1 to 100.00, inclusive.
     */
    ZDITHER0(HduType.ANY, ValueType.INTEGER, ""),

    /**
     * When using the quantization method to compress floating-point images, this header is used to store the integer
     * value that represents undefined pixels (if any) in the scaled integer pixel values. These pixels have an IEEE NaN
     * value (Not a Number) in the uncompressed floating-point image. The recommended value for ZBLANK is -2147483648
     * (the largest negative 32-bit integer).
     */
    ZBLANK(HduType.ANY, ValueType.INTEGER, ""),

    /**
     * The value field of this keyword shall contain an integer representing the number of rows of data from the
     * original binary table that are contained in each tile of the compressed table. The number of rows in the last
     * tile may be less than in the previous tiles. Note that if the entire table is compressed as a single tile, then
     * the compressed table will only contains a single row, and the ZTILELEN and ZNAXIS2 keywords will have the same
     * value.
     */
    ZTILELEN(HduType.ANY, ValueType.INTEGER, ""),

    /**
     * The value field of these keywords shall contain the character string values of the corresponding TFORMn keywords
     * that defines the data type of column n in the original uncompressed FITS table.
     */
    ZFORMn(HduType.ANY, ValueType.STRING, "", FitsKeyword.TFORMn),

    /**
     * The value field of these keywords shall contain a charac- ter string giving the mnemonic name of the algorithm
     * that was used to compress column n of the table. The current allowed values are GZIP_1, GZIP_2, and RICE_1, and
     * the corresponding algorithms
     */
    ZCTYPn(HduType.ANY, ValueType.STRING, "");

    private val header = FitsHeaderKeyItem(name, hduType, valueType, comment)

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

        /**
         * This is the simplest option in which no dithering is performed. The floating-point pixels are simply quantized
         * using Eq. 1. This option should be assumed if the ZQUANTIZ keyword is not present in the header of the compressed
         * floating-point image.
         */
        const val ZQUANTIZ_NO_DITHER = "NO_DITHER"

        /**
         * It should be noted that an image that is quantized using this technique can stil l be unquantized using the
         * simple linear scaling function given by Eq. 1. The only side effect in this ca se is to introduce slightly more
         * noise in the image than if the full subtractive dithering algorith m were applied.
         */
        const val ZQUANTIZ_SUBTRACTIVE_DITHER_1 = "SUBTRACTIVE_DITHER_1"

        /**
         * This dithering algorithm is identical to the SUBTRACTIVE DITHER 1 algorithm described above, ex- cept that any
         * pixels in the floating-point image that are equa l to 0.0 are represented by the reserved value -2147483647 in
         * the quantized integer array. When the i mage is subsequently uncompressed and unscaled, these pixels are restored
         * to their original va lue of 0.0. This dithering option is useful if the zero-valued pixels have special
         * significance to the da ta analysis software, so that the value of these pixels must not be dithered.
         */
        const val ZQUANTIZ_SUBTRACTIVE_DITHER_2 = "SUBTRACTIVE_DITHER_2"

        /**
         * Gzip is the compression algorithm used in the free GN U software utility of the same name. It was created by
         * Jean-loup Gailly and Mark Adler and is based on the DEFLATE algorithm, which is a combination of LZ77 and Huffman
         * coding. DEFLATE was intended as a replacement for LZW and other patent-encumbered data compression algor ithms
         * which, at the time, limited the usability of compress and other popular archivers. Furt her information about
         * this compression technique is readily available on the Internet. The gzip alg orithm has no associated parameters
         * that need to be specified with the ZNAMEn and ZVALn keywords.
         */
        const val ZCMPTYPE_GZIP_1 = "GZIP_1"

        /**
         * If ZCMPTYPE = ’GZIP 2’ then the bytes in the array of image pixel values are shuffled in to decreasing order of
         * significance before being compressed with the gzip algorithm. In other words, bytes are shuffled so that the most
         * significant byte of every pixel occurs first, in order, followed by the next most significant byte, and so on for
         * every byte. Since the most significan bytes of the pixel values often have very similar values, grouping them
         * together in this way often achieves better net compression of the array. This is usually especially effective
         * when compressing floating-point arrays.
         */
        const val ZCMPTYPE_GZIP_2 = "GZIP_2"

        /**
         * If ZCMPTYPE = ’RICE 1’ then the Rice algorithm is used to compress and uncompress the image pixels. The Rice
         * algorithm (Rice, R. F., Yeh, P.-S., and Miller, W. H. 1993, in Proc. of the 9th AIAA Computing in Aerospace
         * Conf., AIAA-93-4541-CP, American Institute of Aeronautics and Astronautics) is simple and very fast, compressing
         * or decompressing 10 7 pixels/sec on modern workstations. It requires only enough memory to hold a single block of
         * 16 or 32 pixels at a time. It codes the pixels in small blocks and so is able to adapt very quickly to changes in
         * the input image statistics (e.g., Rice has no problem handling cosmic rays, bright stars, saturated pixels,
         * etc.).
         */
        const val ZCMPTYPE_RICE_1 = "RICE_1"

        /**
         * If ZCMPTYPE = ’PLIO 1’ then the IRAF PLIO (Pixel List) algorithm is used to compress and uncompress the image
         * pixels. The PLIO algorithm was developed to store integer-valued image masks in a compressed form. Typical uses
         * of image masks are to segment images into regions, or to mark bad pixels. Such masks often have large regions of
         * constant value hence are highly compressible. The compression algorithm used is based on run-length encoding,
         * with the ability to dynamically follow level changes in the image, allowing a 16-bit encoding to be used
         * regardless of the image depth. The worst case performance occurs when successive pixels have different values.
         * Even in this case the encoding will only require one word (16 bits) per mask pixel, provided either the delta
         * intensity change between pixels is usually less than 12 bits, or the mask represents a zero floored step function
         * of constant height. The worst case cannot exceed npix*2 words provided the mask depth is 24 bits or less.
         */
        const val ZCMPTYPE_PLIO_1 = "PLIO_1"

        /**
         * Hcompress is an the image compression package written by Richard L. White for use at the Space Telescope Science
         * Institute. Hcompress was used to compress the STScI Digitized Sky Survey and has also been used to compress the
         * preview images in the Hubble Data Archive. Briefly, the method used is: <br></br>
         * 1. a wavelet transform called the H-transform (a Haar transform generalized to two dimensions), followed by<br></br>
         * 2. quantization that discards noise in the image while retaining the signal on all scales, followed by 10<br></br>
         * 3. quadtree coding of the quantized coefficients.<br></br>
         * The technique gives very good compression for astronomical images and is relatively fast. The calculations are
         * carried out using integer arithmetic and a re entirely reversible. Consequently, the program can be used for
         * either lossy or lossless compression , with no special approach needed for the lossless case (e.g. there is no
         * need for a file of residuals .)
         */
        const val ZCMPTYPE_HCOMPRESS_1 = "HCOMPRESS_1"
    }
}
