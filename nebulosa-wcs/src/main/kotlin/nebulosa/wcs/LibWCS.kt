package nebulosa.wcs

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.DoubleByReference
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import nebulosa.io.resource
import nebulosa.io.transferAndClose
import oshi.PlatformEnum
import oshi.SystemInfo
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.outputStream

// https://www.atnf.csiro.au/people/mcalabre/WCS/index.html
// https://www.atnf.csiro.au/people/mcalabre/WCS/Intro/WCS01.html

internal interface LibWCS : Library {

    fun wcspih(
        header: String, nkeyrec: Int, relax: Int, ctrl: Int,
        nreject: IntByReference, nwcs: IntByReference, wcs: PointerByReference,
    ): Int

    fun wcsp2s(
        wcs: Pointer,
        ncoord: Int,
        nelem: Int,
        pixcrd: DoubleArray,
        imgcrd: DoubleArray,
        phi: DoubleByReference,
        theta: DoubleByReference,
        world: DoubleArray,
        stat: IntArray,
    ): Int

    fun wcss2p(
        wcs: Pointer,
        ncoord: Int,
        nelem: Int,
        world: DoubleArray,
        phi: DoubleByReference,
        theta: DoubleByReference,
        imgcrd: DoubleArray,
        pixcrd: DoubleArray,
        stat: IntArray,
    ): Int

    fun wcsfree(wcs: Pointer): Int

    companion object {

        internal val INSTANCE: LibWCS by lazy {
            val name = when (val platform = SystemInfo.getCurrentPlatform()) {
                PlatformEnum.LINUX -> "libwcs.so"
                PlatformEnum.WINDOWS -> "libwcs.dll"
                else -> throw IllegalStateException("unsupported platform: $platform")
            }

            val outputDir = System.getProperty("LIBWCS_DIR")?.ifBlank { null }
                ?: System.getProperty("java.io.tmpdir")
            val outputPath = Path.of(outputDir, name)

            if (!outputPath.exists()) {
                resource(name)!!.transferAndClose(outputPath.outputStream())
            }

            Native.load("$outputPath", LibWCS::class.java)
        }
    }
}
