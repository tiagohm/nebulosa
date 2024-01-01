package nebulosa.imaging.algorithms.transformation

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.TransformAlgorithm

data class Debayer(private val pattern: CfaPattern? = null) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        return if (source.mono) {
            process(source.color())
        } else {
            process(source)
        }
    }

    private fun process(source: Image): Image {
        val pattern = requireNotNull(pattern ?: CfaPattern.from(source.header))
        val cache = Array(2) { FloatArray(source.width) }

        val width = source.width
        val height = source.height
        val widthM1 = width - 1
        val heightM1 = height - 1

        val rgbValues = FloatArray(3)
        val rgbCounters = IntArray(3)

        fun copyCacheToRedChannel(rowIndex: Int, y: Int) {
            if (y >= 1) {
                val cacheIndex = y.inv() and 1
                val startIndex = rowIndex - width

                for (x in 0 until width) {
                    source.r[startIndex + x] = cache[cacheIndex][x]
                }
            }
        }

        for (y in 0 until height) {
            val rowIndex = source.indexAt(0, y)

            for (x in 0 until width) {
                val index = rowIndex + x

                rgbValues.fill(0f)
                rgbCounters.fill(0)

                var bayerIndex = pattern[y and 1, x and 1]
                rgbValues[bayerIndex.offset] += source.readRed(index)
                rgbCounters[bayerIndex.offset]++

                if (x != 0) {
                    bayerIndex = pattern[y and 1, x - 1 and 1]
                    rgbValues[bayerIndex.offset] += source.readRed(index - 1)
                    rgbCounters[bayerIndex.offset]++
                }

                if (x != widthM1) {
                    bayerIndex = pattern[y and 1, x + 1 and 1]
                    rgbValues[bayerIndex.offset] += source.readRed(index + 1)
                    rgbCounters[bayerIndex.offset]++
                }

                if (y != 0) {
                    bayerIndex = pattern[y - 1 and 1, x and 1]
                    rgbValues[bayerIndex.offset] += source.readRed(index - source.stride)
                    rgbCounters[bayerIndex.offset]++

                    if (x != 0) {
                        bayerIndex = pattern[y - 1 and 1, x - 1 and 1]
                        rgbValues[bayerIndex.offset] += source.readRed(index - source.stride - 1)
                        rgbCounters[bayerIndex.offset]++
                    }

                    if (x != widthM1) {
                        bayerIndex = pattern[y - 1 and 1, x + 1 and 1]
                        rgbValues[bayerIndex.offset] += source.readRed(index - source.stride + 1)
                        rgbCounters[bayerIndex.offset]++
                    }
                }

                if (y != heightM1) {
                    bayerIndex = pattern[y + 1 and 1, x and 1]
                    rgbValues[bayerIndex.offset] += source.readRed(index + source.stride)
                    rgbCounters[bayerIndex.offset]++

                    if (x != 0) {
                        bayerIndex = pattern[y + 1 and 1, x - 1 and 1]
                        rgbValues[bayerIndex.offset] += source.readRed(index + source.stride - 1)
                        rgbCounters[bayerIndex.offset]++
                    }

                    if (x != widthM1) {
                        bayerIndex = pattern[y + 1 and 1, x + 1 and 1]
                        rgbValues[bayerIndex.offset] += source.readRed(index + source.stride + 1)
                        rgbCounters[bayerIndex.offset]++
                    }
                }

                cache[y and 1][x] = rgbValues[0] / rgbCounters[0]
                source.g[index] = rgbValues[1] / rgbCounters[1]
                source.b[index] = rgbValues[2] / rgbCounters[2]
            }

            copyCacheToRedChannel(rowIndex, y)
        }

        copyCacheToRedChannel(width * height, height.inv() and 1)

        return source
    }
}
