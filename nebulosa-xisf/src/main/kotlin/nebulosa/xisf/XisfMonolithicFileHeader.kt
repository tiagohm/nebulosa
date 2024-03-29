package nebulosa.xisf

import nebulosa.image.format.HeaderCard
import nebulosa.io.ByteOrder

sealed interface XisfMonolithicFileHeader {

    enum class SampleFormat(val byteLength: Long) {
        UINT8(1L),
        UINT16(2L),
        UINT32(4L),
        UINT64(8L),
        FLOAT32(4L),
        FLOAT64(8L),
    }

    enum class ColorSpace {
        GRAY,
        RGB,
        CIELAB,
    }

    enum class PixelStorageModel {
        PLANAR,
        NORMAL,
    }

    enum class CompressionType {
        ZLIB,
        LZ4,
        LZ4_HC,
        ZSTD,
    }

    data class CompressionFormat(
        @JvmField val type: CompressionType,
        @JvmField val shuffled: Boolean,
        @JvmField val uncompressedSize: Long,
        @JvmField val itemSize: Int = 0,
    ) {

        companion object {

            @JvmStatic
            fun parse(text: String): CompressionFormat {
                val parts = text.split(":")

                val type = when (parts[0]) {
                    "zlib", "zlib+sh" -> CompressionType.ZLIB
                    "lz4", "lz4+sh" -> CompressionType.LZ4
                    "lz4hc", "lz4hc+sh" -> CompressionType.LZ4_HC
                    "zstd", "zstd+sh" -> CompressionType.ZSTD
                    else -> throw IllegalArgumentException("Invalid compression type: ${parts[0]}")
                }

                val uncompressedSize = parts[1].toLong()
                val shuffled = parts[0].endsWith("+sh")
                val itemSize = if (shuffled && parts.size >= 3) parts[2].toInt() else 0

                return CompressionFormat(type, shuffled, uncompressedSize, itemSize)
            }
        }
    }

    data class Image(
        @JvmField val width: Int, @JvmField val height: Int, @JvmField val numberOfChannels: Int,
        @JvmField val position: Long, @JvmField val size: Long,
        @JvmField val sampleFormat: SampleFormat, @JvmField val colorSpace: ColorSpace,
        @JvmField val pixelStorage: PixelStorageModel, @JvmField val byteOrder: ByteOrder,
        @JvmField val compressionFormat: CompressionFormat? = null,
        @JvmField val bounds: ClosedFloatingPointRange<Float> = DEFAULT_BOUNDS,
        @JvmField val keywords: List<HeaderCard> = emptyList(),
        @JvmField val thumbnail: Image? = null,
    ) : XisfMonolithicFileHeader

    companion object {

        @JvmStatic val DEFAULT_BOUNDS = 0f..1f
    }
}
