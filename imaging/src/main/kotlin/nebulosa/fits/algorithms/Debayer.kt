package nebulosa.fits.algorithms

import nebulosa.fits.Image

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
                val index = y * source.stride + x * source.pixelStride

                rgbValues.fill(0f)
                rgbCounters.fill(0)

                var bayerIndex = pattern[y and 1, x and 1]
                rgbValues[bayerIndex.ordinal] += source.data[index]
                rgbCounters[bayerIndex.ordinal]++

                if (x != 0) {
                    bayerIndex = pattern[y and 1, (x - 1) and 1]
                    rgbValues[bayerIndex.ordinal] += source.data[index - source.pixelStride]
                    rgbCounters[bayerIndex.ordinal]++
                }

                if (x != widthM1) {
                    bayerIndex = pattern[y and 1, (x + 1) and 1]
                    rgbValues[bayerIndex.ordinal] += source.data[index + source.pixelStride]
                    rgbCounters[bayerIndex.ordinal]++
                }

                if (y != 0) {
                    bayerIndex = pattern[(y - 1) and 1, x and 1]
                    rgbValues[bayerIndex.ordinal] += source.data[index - source.stride]
                    rgbCounters[bayerIndex.ordinal]++

                    if (x != 0) {
                        bayerIndex = pattern[(y - 1) and 1, (x - 1) and 1]
                        rgbValues[bayerIndex.ordinal] += source.data[index - source.stride - source.pixelStride]
                        rgbCounters[bayerIndex.ordinal]++
                    }

                    if (x != widthM1) {
                        bayerIndex = pattern[(y - 1) and 1, (x + 1) and 1]
                        rgbValues[bayerIndex.ordinal] += source.data[index - source.stride + source.pixelStride]
                        rgbCounters[bayerIndex.ordinal]++
                    }
                }

                if (y != heightM1) {
                    bayerIndex = pattern[(y + 1) and 1, x and 1]
                    rgbValues[bayerIndex.ordinal] += source.data[index + source.stride]
                    rgbCounters[bayerIndex.ordinal]++

                    if (x != 0) {
                        bayerIndex = pattern[(y + 1) and 1, (x - 1) and 1]
                        rgbValues[bayerIndex.ordinal] += source.data[index + source.stride - source.pixelStride]
                        rgbCounters[bayerIndex.ordinal]++
                    }

                    if (x != widthM1) {
                        bayerIndex = pattern[(y + 1) and 1, (x + 1) and 1]
                        rgbValues[bayerIndex.ordinal] += source.data[index + source.stride + source.pixelStride]
                        rgbCounters[bayerIndex.ordinal]++
                    }
                }

                cachedRed[index / 3] = rgbValues[0] / rgbCounters[0]
                source.data[index + 1] = rgbValues[1] / rgbCounters[1]
                source.data[index + 2] = rgbValues[2] / rgbCounters[2]
            }
        }

        for (i in cachedRed.indices) source.data[i * source.pixelStride] = cachedRed[i]
    }
}
