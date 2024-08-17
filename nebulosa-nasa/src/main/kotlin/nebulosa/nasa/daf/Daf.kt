package nebulosa.nasa.daf

import nebulosa.io.*
import okio.Buffer
import okio.BufferedSource
import java.io.IOException

abstract class Daf : AutoCloseable {

    lateinit var record: FileRecord
        private set

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun component1() = record

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun component2() = summaries

    open fun read() {
        Buffer().use { buffer ->
            // File record.
            buffer.readRecord(1).use { source ->
                // Gets the file format.
                val format = buffer.read(source, 8L) { it.readAscii().uppercase() }

                if (format == "NAIF/DAF") {
                    source.seek(0L)
                    record = buffer.read(source, 88L) { it.parseFileRecord(false) }

                    if (record.nd != 2) {
                        source.seek(0L)
                        record = buffer.read(source, 88L) { it.parseFileRecord(true) }

                        if (record.nd != 2) {
                            throw IOException("neither a big nor a little-endian scan of this file produces the expected ND=2")
                        }
                    }
                } else if (format.startsWith("DAF/")) {
                    source.seek(699L)

                    val magic = buffer.read(source, FTPSTR.length.toLong()) { it.readLatin1() }

                    if (magic != FTPSTR) throw IOException("file has been damaged")

                    source.seek(88L)
                    val littleEndian = buffer.read(source, 8L) { it.readAscii().uppercase() == "LTL-IEEE" }

                    source.seek(0L)
                    record = buffer.read(source, 88L) { it.parseFileRecord(littleEndian) }
                } else {
                    throw IOException("unsupported format: $format")
                }
            }
        }
    }

    abstract fun read(start: Int, end: Int): DoubleArray

    protected abstract fun Buffer.readRecord(index: Int): SeekableSource

    val summaries: List<Summary> by lazy {
        val summaries = ArrayList<Summary>()
        var recordNumber = record.fward

        Buffer().use { buffer ->
            val length = record.nd * 8L + record.ni * 4L
            val step = length - length % 8

            while (recordNumber != 0) {
                buffer.readRecord(recordNumber).use { source ->
                    val sc = buffer.read(source, 24L) { it.parseSummaryControlRecord() }
                    summaries.addAll(parseSummaries(recordNumber, sc.numberOfSummaries, source, step))
                    recordNumber = sc.nextNumber
                }
            }

            summaries
        }
    }

    private fun parseSummaries(
        recordNumber: Int,
        numberOfSummaries: Int,
        data: SeekableSource,
        step: Long,
        // length: Long,
    ): List<Summary> {
        val summaries = ArrayList<Summary>()

        Buffer().use { buffer ->
            buffer.readRecord(recordNumber + 1).use { source ->
                val elementRecordSizeInBytes = record.nd * 8L + record.ni * 4L

                for (i in 0 until numberOfSummaries * step step step) {
                    source.seek(i)
                    val name = buffer.read(source, step).use { it.readAscii().trim() }

                    data.seek(24 + i)
                    val elements = buffer.read(data, elementRecordSizeInBytes) { it.parseElementRecords() }
                    summaries.add(Summary(name, elements.first, elements.second))
                }
            }
        }

        return summaries
    }

    private fun BufferedSource.parseSummaryControlRecord(): SummaryControlRecord {
        val next = readDouble(record.order).toInt()
        val prev = readDouble(record.order).toInt()
        val nsum = readDouble(record.order).toInt()
        return SummaryControlRecord(next, prev, nsum)
    }

    private fun BufferedSource.parseElementRecords(): Pair<DoubleArray, IntArray> {
        val doubles = DoubleArray(record.nd)
        val ints = IntArray(record.ni)
        for (index in doubles.indices) doubles[index] = readDouble(record.order)
        for (index in ints.indices) ints[index] = readInt(record.order)
        return Pair(doubles, ints)
    }

    companion object {

        private const val FTPSTR = "FTPSTR:\r:\n:\r\n:\r\u0000:\u0081:\u0010\u00CE:ENDFTP"

        private fun BufferedSource.parseFileRecord(littleEndian: Boolean): FileRecord {
            val order = if (littleEndian) ByteOrder.LITTLE else ByteOrder.BIG
            // An identification word.
            // val locidw = data.string(8)
            skip(8)
            // The number of double precision components in each array summary.
            val nd = readInt(order)
            // The number of integer components in each array summary.
            val ni = readInt(order)
            // The internal name or description of the array file.
            // val locifn = string(60)
            skip(60)
            // The record number of the initial summary record in the file.
            val fward = readInt(order)
            // The record number of the final summary record in the file.
            val bward = readInt(order)
            // The first free address in the file.
            // val free = readInt()
            return FileRecord(nd, ni, fward, bward, order)
        }
    }
}
