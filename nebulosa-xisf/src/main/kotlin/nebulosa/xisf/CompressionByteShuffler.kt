package nebulosa.xisf

/**
 * For data blocks structured as contiguous sequences of 16-bit or larger integer
 * or floating point numbers, a reversible byte shuffling routine can greatly improve
 * compression ratios by increasing data locality, i.e. by redistributing the
 * sequence such that similar byte values tend to be placed close together.
 */
object CompressionByteShuffler {

    fun shuffle(input: ByteArray, output: ByteArray, itemSize: Int) {
        val size = input.size
        val numberOfItems = size / itemSize
        val copyLength = size % itemSize

        var s = 0

        for (j in 0 until itemSize) {
            var u = j

            repeat(numberOfItems) {
                output[s++] = input[u]
                u += itemSize
            }

            if (copyLength > 0) {
                val destinationOffset = s
                val startIndex = numberOfItems * itemSize
                input.copyInto(output, destinationOffset, startIndex, startIndex + copyLength)
            }
        }
    }

    fun unshuffle(input: ByteArray, output: ByteArray, itemSize: Int) {
        val size = input.size
        val numberOfItems = size / itemSize
        val copyLength = size % itemSize

        var s = 0

        for (j in 0 until itemSize) {
            var u = j

            repeat(numberOfItems) {
                output[u] = input[s++]
                u += itemSize
            }

            if (copyLength > 0) {
                val destinationOffset = numberOfItems * itemSize
                val startIndex = s
                input.copyInto(output, destinationOffset, startIndex, startIndex + copyLength)
            }
        }
    }
}
