package nebulosa.wcs

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.DoubleByReference
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference

// https://www.atnf.csiro.au/people/mcalabre/WCS/index.html
// https://www.atnf.csiro.au/people/mcalabre/WCS/Intro/WCS01.html

interface LibWCS : Library {

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

        const val LIBRARY_NAME = "libwcs"
        const val PATH = "LIBWCS_PATH"

        @JvmStatic val INSTANCE: LibWCS by lazy { Native.load(System.getProperty(PATH, LIBRARY_NAME), LibWCS::class.java) }
    }
}
