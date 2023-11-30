package nebulosa.test

import nebulosa.fits.Fits
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.hips2fits.HipsSurvey
import nebulosa.io.transferAndCloseOutput
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.hours
import okio.ByteString.Companion.toByteString
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.outputStream

abstract class Hips2FitsStringSpec : FitsStringSpec() {

    protected val M31 by lazy { Fits(download("00 42 44.3".hours, "41 16 9".deg, 3.deg)).also(Fits::read) }

    protected fun download(centerRA: Angle, centerDEC: Angle, fov: Angle): Path {
        val name = "$centerRA@$centerDEC@$fov".toByteArray().toByteString().md5().hex()
        val path = Path.of(System.getProperty("java.io.tmpdir"), name)

        if (path.exists() && path.fileSize() > 0L) {
            return path
        }

        HIPS_SERVICE
            .query(CDS_P_DSS2_NIR, centerRA, centerDEC, 1280, 720, 0.0, fov)
            .execute()
            .body()!!
            .use { it.byteStream().transferAndCloseOutput(path.outputStream()) }

        return path
    }

    companion object {

        @JvmStatic val HIPS_SERVICE = Hips2FitsService(httpClient = HTTP_CLIENT)
        @JvmStatic val CDS_P_DSS2_NIR = HipsSurvey("CDS/P/DSS2/NIR")
    }
}
