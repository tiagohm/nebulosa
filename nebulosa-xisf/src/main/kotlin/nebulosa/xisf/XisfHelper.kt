@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.xisf

import nebulosa.xisf.XisfFormat.readSignature
import okio.buffer
import okio.source
import java.io.File
import java.nio.file.Path

inline fun Path.xisf() = XisfPath(this).also(XisfPath::read)

inline fun File.xisf() = XisfPath(this).also(XisfPath::read)

inline fun File.isXisf() = source().buffer().use { it.readSignature() == XisfFormat.SIGNATURE }

inline fun Path.isXisf() = source().buffer().use { it.readSignature() == XisfFormat.SIGNATURE }
