package nebulosa.xisf

import nebulosa.fits.Bitpix
import nebulosa.fits.FitsHeader
import nebulosa.fits.FitsHeaderCard
import nebulosa.fits.FitsKeyword
import nebulosa.image.format.HeaderCard
import nebulosa.io.ByteOrder

sealed interface XisfMonolithicFileHeader {

    enum class SampleFormat(
        @JvmField val byteLength: Long,
        @JvmField val code: String, @JvmField val bitpix: Bitpix,
    ) {
        UINT8(1L, "UInt8", Bitpix.BYTE),
        UINT16(2L, "UInt16", Bitpix.SHORT),
        UINT32(4L, "UInt32", Bitpix.INTEGER),
        UINT64(8L, "UInt64", Bitpix.LONG),
        FLOAT32(4L, "Float32", Bitpix.FLOAT),
        FLOAT64(8L, "Float64", Bitpix.DOUBLE);

        companion object {

            @JvmStatic
            fun from(bitpix: Bitpix) = when (bitpix) {
                Bitpix.BYTE -> UINT8
                Bitpix.SHORT -> UINT16
                Bitpix.INTEGER -> UINT32
                Bitpix.LONG -> UINT64
                Bitpix.FLOAT -> FLOAT32
                Bitpix.DOUBLE -> FLOAT64
            }
        }
    }

    enum class ColorSpace(@JvmField val code: String) {
        GRAY("Gray"),
        RGB("RGB"),
        CIELAB("CIELab"),
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

    enum class ImageType(@JvmField val code: String) : HeaderCard by FitsHeaderCard.create(FitsKeyword.IMAGETYP, code) {
        BIAS("Bias"),
        DARK("Dark"),
        FLAT("Flat"),
        LIGHT("Light"),
        MASTER_BIAS("MasterBias"),
        MASTER_DARK("MasterDark"),
        MASTER_FLAT("MasterFlat"),
        MASTER_LIGHT("MasterLight"),
        DEFECT_MAP("DefectMap"),
        REJECTION_MAP_HIGH("RejectionMapHigh"),
        REJECTION_MAP_LOW("RejectionMapLow"),
        BINARY_REJECTION_MAP_HIGH("BinaryRejectionMapHigh"),
        BINARY_REJECTION_MAP_LOW("BinaryRejectionMapLow"),
        SLOPE_MAP("SlopeMap"),
        WEIGHT_MAP("WeightMap");

        companion object {

            @JvmStatic private val MAPPED = entries.associateBy { it.value }

            @JvmStatic
            fun parse(text: String) = MAPPED[text]
        }
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

    @Suppress("ArrayInDataClass")
    data class Image(
        @JvmField val width: Int, @JvmField val height: Int, @JvmField val numberOfChannels: Int,
        @JvmField val position: Long, @JvmField val size: Long,
        @JvmField val sampleFormat: SampleFormat, @JvmField val colorSpace: ColorSpace,
        @JvmField val pixelStorage: PixelStorageModel, @JvmField val byteOrder: ByteOrder,
        @JvmField val compressionFormat: CompressionFormat? = null,
        @JvmField val imageType: ImageType = ImageType.LIGHT,
        @JvmField val bounds: ClosedFloatingPointRange<Float> = DEFAULT_BOUNDS,
        @JvmField val keywords: FitsHeader = FitsHeader.EMPTY,
        @JvmField val thumbnail: Image? = null,
        @JvmField val embedded: ByteArray = ByteArray(0),
    ) : XisfMonolithicFileHeader

    companion object {

        @JvmStatic val DEFAULT_BOUNDS = 0f..1f
    }
}
