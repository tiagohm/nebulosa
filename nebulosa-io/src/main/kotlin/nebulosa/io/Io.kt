@file:JvmName("Io")

package nebulosa.io

import okio.*
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.URL
import java.nio.ByteBuffer
import java.util.*

val EMPTY_BYTE_ARRAY = ByteArray(0)

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readUnsignedByte() = readByte().toInt() and 0xff

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readShort(order: ByteOrder) = if (order.isBigEndian) readShort() else readShortLe()

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readInt(order: ByteOrder) = if (order.isBigEndian) readInt() else readIntLe()

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readLong(order: ByteOrder) = if (order.isBigEndian) readLong() else readLongLe()

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readFloat(order: ByteOrder) = if (order.isBigEndian) readFloat() else readFloatLe()

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readDouble(order: ByteOrder) = if (order.isBigEndian) readDouble() else readDoubleLe()

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readFloat() = Float.fromBits(readInt())

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readFloatLe() = Float.fromBits(readIntLe())

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readDouble() = Double.fromBits(readLong())

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readDoubleLe() = Double.fromBits(readLongLe())

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readDoubleArray(size: Int) = DoubleArray(size) { readDouble() }

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readDoubleArrayLe(size: Int) = DoubleArray(size) { readDoubleLe() }

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readDoubleArray(size: Int, order: ByteOrder) = DoubleArray(size) { readDouble(order) }

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readAscii() = readString(Charsets.US_ASCII)

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readAscii(byteCount: Long) = readString(byteCount, Charsets.US_ASCII)

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readLatin1() = readString(Charsets.ISO_8859_1)

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSource.readLatin1(byteCount: Long) = readString(byteCount, Charsets.ISO_8859_1)

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSink.writeFloat(f: Float) = writeInt(f.toBits())

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSink.writeFloatLe(f: Float) = writeIntLe(f.toBits())

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSink.writeDouble(d: Double) = writeLong(d.toBits())

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSink.writeDoubleLe(d: Double) = writeLongLe(d.toBits())

@Suppress("NOTHING_TO_INLINE")
inline fun resource(name: String): InputStream? = Thread.currentThread().contextClassLoader.getResourceAsStream(name)

@Suppress("NOTHING_TO_INLINE")
inline fun resourceUrl(name: String): URL? = Thread.currentThread().contextClassLoader.getResource(name)

@Suppress("NOTHING_TO_INLINE")
inline fun bufferedResource(name: String) = resource(name)?.source()?.buffer()

inline fun <R> bufferedResource(name: String, block: BufferedSource.() -> R) = bufferedResource(name)!!.use(block)

inline val Buffer.UnsafeCursor.remaining
    get() = end - start

fun ByteArray.source(
    offset: Int = 0,
    byteCount: Int = size - offset,
    timeout: Timeout = Timeout.NONE,
): SeekableSource = ByteArraySource(this, offset, byteCount, timeout)

fun ByteArray.sink(
    offset: Int = 0,
    byteCount: Int = size - offset,
    timeout: Timeout = Timeout.NONE,
): SeekableSink = ByteArraySink(this, offset, byteCount, timeout)

fun ByteBuffer.source(
    offset: Int = 0,
    byteCount: Int = capacity() - offset,
    timeout: Timeout = Timeout.NONE,
): SeekableSource = ByteBufferSource(this, offset, byteCount, timeout)


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

fun RandomAccessFile.sink(
    timeout: Timeout = Timeout.NONE,
): SeekableSink = RandomAccessFileSink(this, timeout)

fun File.seekableSink(
    timeout: Timeout = Timeout.NONE,
): SeekableSink = RandomAccessFile(this, "rw").sink(timeout)
