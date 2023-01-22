package nebulosa.io

enum class ByteOrder(@JvmField val isBigEndian: Boolean) {
    BIG(true),
    LITTLE(false);
}
