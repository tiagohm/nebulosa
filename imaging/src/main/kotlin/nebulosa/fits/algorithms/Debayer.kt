package nebulosa.fits.algorithms

import nebulosa.fits.Image

class Debayer(val pattern: CfaPattern = CfaPattern.GRGB) : PartialTransformAlgorithm {

    override fun transform(source: Image): Image {
        if (source.mono) {
            TODO("") // create RGB image, copy gray to red channel e call super.transform.
        } else {
            return super.transform(source)
        }
    }

    override fun transform(source: Image, destination: Image) {
        val width = source.width
        val height = source.height
        val widthM1 = width - 1
        val heightM1 = height - 1

        val rgbValues = FloatArray(3)
        val rgbCounters = IntArray(3)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixelIndex = y * source.stride + x * source.pixelStride

                rgbValues.fill(0f)
                rgbCounters.fill(0)

                var bayerIndex = pattern[y and 1, x and 1]
                rgbValues[bayerIndex.ordinal] += source.data[pixelIndex]
                rgbCounters[bayerIndex.ordinal]++

                if (x != 0) {
                    bayerIndex = pattern[y and 1, (x - 1) and 1]
                    rgbValues[bayerIndex.ordinal] += source.data[pixelIndex - source.pixelStride]
                    rgbCounters[bayerIndex.ordinal]++
                }

                if (x != widthM1) {
                    bayerIndex = pattern[y and 1, (x + 1) and 1]
                    rgbValues[bayerIndex.ordinal] += source.data[pixelIndex + source.pixelStride]
                    rgbCounters[bayerIndex.ordinal]++
                }

                if (y != 0) {
                    bayerIndex = pattern[(y - 1) and 1, x and 1]
                    rgbValues[bayerIndex.ordinal] += source.data[pixelIndex - source.stride]
                    rgbCounters[bayerIndex.ordinal]++

                    if (x != 0) {
                        bayerIndex = pattern[(y - 1) and 1, (x - 1) and 1]
                        rgbValues[bayerIndex.ordinal] += source.data[pixelIndex - source.stride - source.pixelStride]
                        rgbCounters[bayerIndex.ordinal]++
                    }

                    if (x != widthM1) {
                        bayerIndex = pattern[(y - 1) and 1, (x + 1) and 1]
                        rgbValues[bayerIndex.ordinal] += source.data[pixelIndex - source.stride + source.pixelStride]
                        rgbCounters[bayerIndex.ordinal]++
                    }
                }

                if (y != heightM1) {
                    bayerIndex = pattern[(y + 1) and 1, x and 1]
                    rgbValues[bayerIndex.ordinal] += source.data[pixelIndex + source.stride]
                    rgbCounters[bayerIndex.ordinal]++

                    if (x != 0) {
                        bayerIndex = pattern[(y + 1) and 1, (x - 1) and 1]
                        rgbValues[bayerIndex.ordinal] += source.data[pixelIndex + source.stride - source.pixelStride]
                        rgbCounters[bayerIndex.ordinal]++
                    }

                    if (x != widthM1) {
                        bayerIndex = pattern[(y + 1) and 1, (x + 1) and 1]
                        rgbValues[bayerIndex.ordinal] += source.data[pixelIndex + source.stride + source.pixelStride]
                        rgbCounters[bayerIndex.ordinal]++
                    }
                }

                for (i in 0..2) destination.data[pixelIndex + i] = rgbValues[i] / rgbCounters[i]
            }
        }
    }
}
