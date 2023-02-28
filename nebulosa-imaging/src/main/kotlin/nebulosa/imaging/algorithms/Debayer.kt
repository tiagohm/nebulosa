package nebulosa.imaging.algorithms

import nebulosa.imaging.Image

class Debayer(val pattern: CfaPattern = CfaPattern.GRGB) : TransformAlgorithm {

    private var cachedRed = FloatArray(0) // Prevent clone image.

    override fun transform(source: Image): Image {
        cachedRed = FloatArray(source.width * source.height)

        if (source.mono) {
            TODO("") // create RGB image, copy gray to red channel e call transform.
        } else {
            process(source)
        }

        return source
    }

    private fun process(source: Image) {
        val width = source.width
        val height = source.height
        val widthM1 = width - 1
        val heightM1 = height - 1

        val rgbValues = FloatArray(3)
        val rgbCounters = IntArray(3)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = source.indexAt(x, y)

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

                cachedRed[index] = rgbValues[0] / rgbCounters[0]
                source.g[index] = rgbValues[1] / rgbCounters[1]
                source.b[index] = rgbValues[2] / rgbCounters[2]
            }
        }

        cachedRed.copyInto(source.r)
    }
}
