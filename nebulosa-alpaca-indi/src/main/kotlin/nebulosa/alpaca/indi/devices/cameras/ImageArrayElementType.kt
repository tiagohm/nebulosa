package nebulosa.alpaca.indi.devices.cameras

enum class ImageArrayElementType {
    UNKNOWN, // 0 to 3 are values already used in the Alpaca standard
    INT16,
    INT32,
    DOUBLE,
    SINGLE, // 4 to 9 are an extension to include other numeric types
    UINT64,
    BYTE,
    INT64,
    UINT16,
    UINT32,

}
