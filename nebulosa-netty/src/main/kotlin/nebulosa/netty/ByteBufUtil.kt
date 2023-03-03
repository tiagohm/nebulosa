package nebulosa.netty

import io.netty.buffer.ByteBuf

@Suppress("NOTHING_TO_INLINE")
inline fun ByteBuf.writeAscii(text: CharSequence) = writeCharSequence(text, Charsets.US_ASCII)
