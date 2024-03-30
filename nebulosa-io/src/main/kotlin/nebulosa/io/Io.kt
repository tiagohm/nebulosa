@file:JvmName("Io")
@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.io

import okio.*
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.net.URL
import java.nio.ByteBuffer
import java.nio.file.Path
import java.util.*

inline fun BufferedSource.readSignedByte() = readByte().toInt()

inline fun BufferedSource.readUnsignedByte() = readSignedByte() and 0xFF

inline fun BufferedSource.readShort(order: ByteOrder) = if (order.isBigEndian) readShort() else readShortLe()

inline fun BufferedSource.readUnsignedShort() = readShort().toInt() and 0xFFFF

inline fun BufferedSource.readUnsignedShortLe() = readShortLe().toInt() and 0xFFFF

inline fun BufferedSource.readUnsignedShort(order: ByteOrder) = readShort(order).toInt() and 0xFFFF

inline fun BufferedSource.readInt(order: ByteOrder) = if (order.isBigEndian) readInt() else readIntLe()

inline fun BufferedSource.readUnsignedInt() = readInt().toLong() and 0xFFFFFFFF

inline fun BufferedSource.readUnsignedIntLe() = readIntLe().toLong() and 0xFFFFFFFF

inline fun BufferedSource.readUnsignedInt(order: ByteOrder) = readInt(order).toLong() and 0xFFFFFFFF

inline fun BufferedSource.readLong(order: ByteOrder) = if (order.isBigEndian) readLong() else readLongLe()

inline fun BufferedSource.readFloat(order: ByteOrder) = if (order.isBigEndian) readFloat() else readFloatLe()

inline fun BufferedSource.readDouble(order: ByteOrder) = if (order.isBigEndian) readDouble() else readDoubleLe()

inline fun BufferedSource.readFloat() = Float.fromBits(readInt())

inline fun BufferedSource.readFloatLe() = Float.fromBits(readIntLe())

inline fun BufferedSource.readDouble() = Double.fromBits(readLong())

inline fun BufferedSource.readDoubleLe() = Double.fromBits(readLongLe())

inline fun BufferedSource.readDoubleArray(size: Int) = DoubleArray(size) { readDouble() }

inline fun BufferedSource.readDoubleArrayLe(size: Int) = DoubleArray(size) { readDoubleLe() }

inline fun BufferedSource.readDoubleArray(size: Int, order: ByteOrder) = DoubleArray(size) { readDouble(order) }

inline fun BufferedSource.readAscii() = readString(Charsets.US_ASCII)

inline fun BufferedSource.readAscii(byteCount: Long) = readString(byteCount, Charsets.US_ASCII)

inline fun BufferedSource.readLatin1() = readString(Charsets.ISO_8859_1)

inline fun BufferedSource.readLatin1(byteCount: Long) = readString(byteCount, Charsets.ISO_8859_1)

inline fun BufferedSink.writeFloat(f: Float) = writeInt(f.toBits())

inline fun BufferedSink.writeFloatLe(f: Float) = writeIntLe(f.toBits())

inline fun BufferedSink.writeDouble(d: Double) = writeLong(d.toBits())

inline fun BufferedSink.writeDoubleLe(d: Double) = writeLongLe(d.toBits())

inline fun ClassLoader.resource(name: String): InputStream? = getResourceAsStream(name)

inline fun resource(name: String): InputStream? = Thread.currentThread().contextClassLoader.resource(name)

inline fun ClassLoader.resourceUrl(name: String): URL? = getResource(name)

inline fun resourceUrl(name: String): URL? = Thread.currentThread().contextClassLoader.resourceUrl(name)

inline fun bufferedResource(name: String) = resource(name)?.source()?.buffer()

inline fun lazyBufferedResource(name: String) = lazy { bufferedResource(name) }

inline fun <R> bufferedResource(name: String, block: BufferedSource.() -> R) = bufferedResource(name)!!.use(block)

inline fun <R> lazyBufferedResource(name: String, crossinline block: BufferedSource.() -> R) = lazy { bufferedResource(name, block) }

inline val Buffer.UnsafeCursor.remaining
    get() = end - start

fun ByteArray.source(
    offset: Int = 0,
    byteCount: Int = size - offset,
    timeout: Timeout = Timeout.NONE,
): SeekableSource = ByteArraySource(this, offset, byteCount.toLong(), timeout)

fun ByteArray.sink(
    offset: Int = 0,
    byteCount: Int = size - offset,
    timeout: Timeout = Timeout.NONE,
): SeekableSink = ByteArraySink(this, offset, byteCount.toLong(), timeout)

fun ByteBuffer.sink(
    offset: Int = 0,
    byteCount: Int = remaining() - offset,
    timeout: Timeout = Timeout.NONE,
): SeekableSink = ByteBufferSink(this, offset, byteCount.toLong(), timeout)

fun ByteBuffer.source(
    offset: Int = 0,
    byteCount: Int = remaining() - offset,
    timeout: Timeout = Timeout.NONE,
): SeekableSource = ByteBufferSource(this, offset, byteCount.toLong(), timeout)

fun Random.source(
    maxSize: Long = Long.MAX_VALUE,
    timeout: Timeout = Timeout.NONE,
): SeekableSource = RandomSource(this, maxSize, timeout)

fun RandomAccessFile.source(
    timeout: Timeout = Timeout.NONE,
): SeekableSource = RandomAccessFileSource(this, timeout)

fun File.seekableSource(
    timeout: Timeout = Timeout.NONE,
): SeekableSource = RandomAccessFile(this, "r").source(timeout)

inline fun Path.seekableSource(
    timeout: Timeout = Timeout.NONE,
) = toFile().seekableSource(timeout)

fun RandomAccessFile.sink(
    timeout: Timeout = Timeout.NONE,
): SeekableSink = RandomAccessFileSink(this, timeout)

inline fun File.seekableSink(
    timeout: Timeout = Timeout.NONE,
): SeekableSink = RandomAccessFile(this, "rw").sink(timeout)

inline fun Path.seekableSink(
    timeout: Timeout = Timeout.NONE,
) = toFile().seekableSink(timeout)

inline fun InputStream.transferAndCloseInput(output: OutputStream) = use { transferTo(output) }

inline fun InputStream.transferAndCloseOutput(output: OutputStream) = output.use(::transferTo)

inline fun InputStream.transferAndClose(output: OutputStream) = use { output.use(::transferTo) }

inline fun Buffer.read(source: Source, byteCount: Long): Buffer {
    source.read(this, byteCount)
    return this
}

inline fun <T> Buffer.read(source: Source, byteCount: Long, block: (Buffer) -> T): T {
    source.read(this, byteCount)

    return try {
        block(this)
    } finally {
        clear()
    }
}

/**
 * Reads [byteCount] bytes from [source] to this buffer.
 */
fun Buffer.readFully(source: Source, byteCount: Long) {
    var remainingCount = byteCount

    while (remainingCount > 0L) {
        val size = source.read(this, remainingCount)
        require(size > 0) { "unexpected end of file" }
        remainingCount -= size
    }
}

/**
 * Transfers [byteCount] bytes from [source] to [sink] using this buffer as intermediate.
 */
fun Buffer.transferFully(source: Source, sink: Sink, byteCount: Long) {
    var remainingCount = byteCount

    while (remainingCount > 0L) {
        val size = source.read(this, remainingCount)
        if (size < 0L) throw EOFException("unexpected end of file")
        sink.write(this, size)
        remainingCount -= size
    }
}
