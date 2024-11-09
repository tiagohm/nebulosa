package nebulosa.wcs

import com.sun.jna.Pointer
import com.sun.jna.ptr.DoubleByReference
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import nebulosa.image.format.ReadableHeader
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.toDegrees

class WCS(header: ReadableHeader) : AutoCloseable {

    private val wcs: Pointer

    init {
        val headerIter = header.iterator()
        val headerText = StringBuffer(2048)
        var keyCount = 0

        while (headerIter.hasNext()) {
            val card = headerIter.next()

            if (isKeywordValid(card.key)) {
                headerText.append(card.formatted())
                keyCount++
            }
        }

        val nreject = IntByReference()
        val nwcs = IntByReference()
        val wcsRef = PointerByReference()

        val status = LibWCS.INSTANCE.wcspih("$headerText", keyCount, 0x000FFFFF, 0, nreject, nwcs, wcsRef)

        if (status == 0 && wcsRef.value != null) {
            wcs = wcsRef.value
        } else {
            throw WCSException("failed to initialize WCS from keywords", status)
        }
    }

    fun pixToSky(x: Double, y: Double): WorldCoordinates {
        val pixcrd = doubleArrayOf(x, y)
        val imgcrd = doubleArrayOf(0.0, 0.0)
        val phi = DoubleByReference(0.0)
        val theta = DoubleByReference(0.0)
        val world = DoubleArray(2) { Double.NaN }
        val stat = IntArray(1)

        val status = LibWCS.INSTANCE.wcsp2s(wcs, 1, 2, pixcrd, imgcrd, phi, theta, world, stat)

        if (status != 0) {
            throw WCSException("failed to transform pixel coordinates to world coordinates", status)
        }

        return WorldCoordinates(world[0].deg, world[1].deg, phi.value.deg, theta.value.deg)
    }

    fun skyToPix(rightAscension: Angle, declination: Angle): PixelCoordinates {
        val world = doubleArrayOf(rightAscension.toDegrees, declination.toDegrees)
        val imgcrd = doubleArrayOf(0.0, 0.0)
        val phi = DoubleByReference(0.0)
        val theta = DoubleByReference(0.0)
        val pixcrd = DoubleArray(2) { Double.NaN }
        val stat = IntArray(1)

        val status = LibWCS.INSTANCE.wcss2p(wcs, 1, 2, world, phi, theta, imgcrd, pixcrd, stat)

        if (status != 0) {
            throw WCSException("failed to transform world coordinates to pixel coordinates", status)
        }

        return PixelCoordinates(pixcrd[0], pixcrd[1], phi.value.deg, theta.value.deg)
    }

    override fun close() {
        LibWCS.INSTANCE.wcsfree(wcs)
    }

    protected fun finalize() {
        close()
    }

    companion object {

        private val CUNIT = "CUNIT[1-2]".toRegex()
        private val CTYPE = "CTYPE[1-2]".toRegex()
        private val CRPIX = "CRPIX[1-2]".toRegex()
        private val CRVAL = "CRVAL[1-2]".toRegex()
        private val PS = "PS\\d_\\d".toRegex()
        private val CD = "CD\\d_\\d".toRegex()
        private val PC = "PC\\d_\\d".toRegex()
        private val CDELT = "CDELT[1-2]".toRegex()
        private val CROTA = "CROTA[1-2]".toRegex()
        private val SIP_ABP = "[AB]P?_\\d_\\d".toRegex()
        private val SIP_ABP_ORDER = "[AB]P?_ORDER".toRegex()
        private val SIP_AB_DMAX = "[AB]_DMAX".toRegex()

        private val KEYWORDS_REGEX =
            arrayOf(CUNIT, CTYPE, CRPIX, CRVAL, PS, CD, PC, CDELT, CROTA)

        private val SIP_KEYWORDS_REGEX =
            arrayOf(SIP_ABP_ORDER, SIP_ABP, SIP_AB_DMAX)

        private val KEYWORDS =
            arrayOf("LONPOLE", "LATPOLE", "RADESYS", "EQUINOX")

        @JvmStatic
        fun isKeywordValid(key: String): Boolean {
            return KEYWORDS_REGEX.any(key::matches)
                    || key in KEYWORDS
                    || SIP_KEYWORDS_REGEX.any(key::matches)
        }
    }
}
