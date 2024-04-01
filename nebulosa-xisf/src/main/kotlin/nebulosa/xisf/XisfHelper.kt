@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.xisf

import java.io.File
import java.nio.file.Path

inline fun String.xisf() = XisfPath(this).also(XisfPath::read)

inline fun Path.xisf() = XisfPath(this).also(XisfPath::read)

inline fun File.xisf() = XisfPath(this).also(XisfPath::read)
