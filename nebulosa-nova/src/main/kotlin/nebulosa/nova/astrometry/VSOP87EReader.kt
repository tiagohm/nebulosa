package nebulosa.nova.astrometry

import nebulosa.io.readDoubleArray
import nebulosa.io.writeDouble
import okio.BufferedSink
import okio.BufferedSource
import okio.Sink
import okio.Source
import okio.buffer
import okio.sink
import okio.source
import kotlin.io.path.Path

// Exponent, XYZ axis, terms
typealias VSOP87ETerms = Array<Array<DoubleArray>>

object VSOP87EReader {

    // All VSOP87E .txt files sum ~6.9 MB
    fun readTxtFormat(source: Source): VSOP87ETerms {
        val buffer = source as? BufferedSource ?: source.buffer()

        var xyz = 0
        var exp = 0
        val terms = Array(6) { Array(3) { DoubleArray(0) } }

        while (!buffer.exhausted()) {
            val line = buffer.readUtf8Line()?.trimStart() ?: break

            if (line.startsWith("VSOP87")) {
                xyz = line[40].code - 49
                exp = line[58].code - 48
                val size = line.substring(59..65).trim().toInt()
                terms[exp][xyz] = DoubleArray(size * 3)
                continue
            }

            val index = (line.substring(4..8).trim().toInt() - 1) * 3
            val a = line.substring(78..95).trim().toDouble()
            val b = line.substring(96..109).trim().toDouble()
            val c = line.substring(110..129).trim().toDouble()
            terms[exp][xyz][index] = a
            terms[exp][xyz][index + 1] = b
            terms[exp][xyz][index + 2] = c
        }

        return terms
    }

    // All VSOP87E .dat files sum ~1.2 MB
    fun readBinaryFormat(source: Source): VSOP87ETerms {
        val buffer = source as? BufferedSource ?: source.buffer()
        val terms = Array(6) { Array(3) { DoubleArray(0) } }

        for (exp in 0..2) {
            for (xyz in 0..5) {
                val numberOfTerms = buffer.readShort().toInt() and 0xFFFF
                terms[xyz][exp] = buffer.readDoubleArray(numberOfTerms)
            }
        }

        return terms
    }

    fun convertTxtToBinaryFormat(source: Source, sink: Sink) {
        val terms = readTxtFormat(source)
        val buffer = sink as? BufferedSink ?: sink.buffer()

        // number of terms: 2 bytes, data: n * 8 bytes]

        for (exp in 0..2) {
            for (xyz in 0..5) {
                val data = terms[xyz][exp]
                buffer.writeShort(data.size)
                data.forEach(buffer::writeDouble)
            }
        }

        buffer.flush()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val planets = arrayOf("EARTH", "JUPITER", "MARS", "MERCURY", "NEPTUNE", "SATURN", "SUN", "URANUS", "VENUS")

        for (planet in planets) {
            val input = Path("./nebulosa-nova/src/main/resources/VSOP87E_$planet.txt").source()
            val output = Path("./nebulosa-nova/src/main/resources/VSOP87E_$planet.dat").sink()

            convertTxtToBinaryFormat(input, output)

            input.close()
            output.close()
        }
    }
}
