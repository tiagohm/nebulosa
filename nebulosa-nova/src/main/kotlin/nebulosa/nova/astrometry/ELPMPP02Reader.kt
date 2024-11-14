package nebulosa.nova.astrometry

import nebulosa.io.readDouble
import nebulosa.io.writeDouble
import okio.BufferedSink
import okio.BufferedSource
import okio.Sink
import okio.Source
import okio.buffer
import okio.sink
import okio.source
import kotlin.io.path.Path

object ELPMPP02Reader {

    data class MainProblem(
        @JvmField val i0: Int,
        @JvmField val i1: Int,
        @JvmField val i2: Int,
        @JvmField val i3: Int,
        @JvmField val a: Double,
        @JvmField val b0: Double,
        @JvmField val b1: Double,
        @JvmField val b2: Double,
        @JvmField val b3: Double,
        @JvmField val b4: Double,
    ) {

        fun writeTo(sink: BufferedSink) {
            sink.writeByte(i0).writeByte(i1).writeByte(i2).writeByte(i3)
            sink.writeDouble(a).writeDouble(b0).writeDouble(b1).writeDouble(b2).writeDouble(b3).writeDouble(b4)
        }

        companion object {

            fun readFrom(source: BufferedSource) = MainProblem(
                source.readByte().toInt(), source.readByte().toInt(), source.readByte().toInt(), source.readByte().toInt(),
                source.readDouble(), source.readDouble(), source.readDouble(), source.readDouble(), source.readDouble(), source.readDouble(),
            )
        }
    }

    data class Pertubation(
        @JvmField val s: Double,
        @JvmField val c: Double,
        @JvmField val i: IntArray,
    ) {

        fun writeTo(sink: BufferedSink) {
            sink.writeDouble(s).writeDouble(c)
            i.forEach(sink::writeByte)
        }

        companion object {

            fun readFrom(source: BufferedSource) = Pertubation(
                source.readDouble(), source.readDouble(),
                IntArray(13) { source.readByte().toInt() },
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun readMainProblemTxtFormat(source: Source): Array<MainProblem> {
        val buffer = if (source is BufferedSource) source else source.buffer()

        var line = buffer.readUtf8LineStrict()
        val n = line.substring(24..34).trim().toInt()
        val data = arrayOfNulls<MainProblem>(n)

        repeat(n) {
            line = buffer.readUtf8LineStrict()

            data[it] = MainProblem(
                line.substring(0..2).trim().toInt(),
                line.substring(3..5).trim().toInt(),
                line.substring(6..8).trim().toInt(),
                line.substring(9..11).trim().toInt(),
                line.substring(14..26).trim().toDouble(),
                line.substring(27..38).trim().toDouble(),
                line.substring(39..50).trim().toDouble(),
                line.substring(51..62).trim().toDouble(),
                line.substring(63..74).trim().toDouble(),
                line.substring(75..86).trim().toDouble(),
            )
        }

        return data as Array<MainProblem>
    }

    fun readMainProblemBinaryFormat(source: Source): Array<MainProblem> {
        val buffer = if (source is BufferedSource) source else source.buffer()
        val n = buffer.readShort().toInt() and 0xFFFF
        return Array(n) { MainProblem.readFrom(buffer) }
    }

    fun convertMainProblemFromTxtToBinaryFormat(source: Source, sink: Sink) {
        val data = readMainProblemTxtFormat(source)
        val buffer = if (sink is BufferedSink) sink else sink.buffer()
        buffer.writeShort(data.size)
        data.forEach { it.writeTo(buffer) }
        buffer.flush()
    }

    fun readPertubationTxtFormat(source: Source): List<Array<Pertubation>> {
        val buffer = if (source is BufferedSource) source else source.buffer()
        val data = ArrayList<Array<Pertubation>>(4)

        while (!buffer.exhausted()) {
            var line = buffer.readUtf8LineStrict()
            val n = line.substring(25..34).trim().toInt()

            Array(n) {
                line = buffer.readUtf8LineStrict()
                val s = line.substring(5..24).trim().replace('D', 'E').toDouble()
                val c = line.substring(25..44).trim().replace('D', 'E').toDouble()
                val i = IntArray(13) { line.substring((45 + it * 3)..(47 + it * 3)).trim().toInt() }
                Pertubation(s, c, i)
            }.also(data::add)
        }

        return data
    }

    fun readPertubationBinaryFormat(source: Source): List<Array<Pertubation>> {
        val buffer = if (source is BufferedSource) source else source.buffer()
        val k = buffer.readShort().toInt() and 0xFFFF
        val data = ArrayList<Array<Pertubation>>(k)

        repeat(k) {
            val n = buffer.readShort().toInt() and 0xFFFF
            data.add(Array(n) { Pertubation.readFrom(buffer) })
        }

        return data
    }

    fun convertPertubationFromTxtToBinaryFormat(source: Source, sink: Sink) {
        val data = readPertubationTxtFormat(source)
        val buffer = if (sink is BufferedSink) sink else sink.buffer()
        buffer.writeShort(data.size)

        data.forEach { e ->
            buffer.writeShort(e.size)
            e.forEach { it.writeTo(buffer) }
        }

        buffer.flush()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        for (s in 1..3) {
            val input = Path("./nebulosa-nova/src/main/resources/ELP_MAIN.S$s.txt").source()
            val output = Path("./nebulosa-nova/src/main/resources/ELP_MAIN.S$s.dat").sink()

            convertMainProblemFromTxtToBinaryFormat(input, output)

            input.close()
            output.close()
        }

        for (s in 1..3) {
            val input = Path("./nebulosa-nova/src/main/resources/ELP_PERT.S$s.txt").source()
            val output = Path("./nebulosa-nova/src/main/resources/ELP_PERT.S$s.dat").sink()

            convertPertubationFromTxtToBinaryFormat(input, output)

            input.close()
            output.close()
        }
    }
}
