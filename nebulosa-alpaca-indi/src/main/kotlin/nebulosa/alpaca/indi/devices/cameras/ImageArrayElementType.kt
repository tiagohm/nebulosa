package nebulosa.alpaca.indi.devices.cameras

import nebulosa.fits.Bitpix

enum class ImageArrayElementType(@JvmField val bitpix: Bitpix) {
    UNKNOWN(Bitpix.BYTE), // 0 to 3 are values already used in the Alpaca standard
    INT16(Bitpix.SHORT),
    INT32(Bitpix.INTEGER),
    DOUBLE(Bitpix.DOUBLE),
    SINGLE(Bitpix.FLOAT), // 4 to 9 are an extension to include other numeric types
    UINT64(Bitpix.LONG),
    BYTE(Bitpix.BYTE),
    INT64(Bitpix.LONG),
    UINT16(Bitpix.SHORT),
    UINT32(Bitpix.INTEGER),
}
