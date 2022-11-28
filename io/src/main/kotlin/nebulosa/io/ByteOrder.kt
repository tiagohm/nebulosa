package nebulosa.io

enum class ByteOrder(val isBigEndian: Boolean) {
    BIG(true),
    LITTLE(false);
}
