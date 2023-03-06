import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.math.Angle.Companion.deg
import nebulosa.wcs.WCSTransform
import kotlin.math.abs

abstract class AbstractWCSTransformTest : StringSpec() {

    protected abstract val header: Map<String, Any>

    protected val wcs by lazy { WCSTransform(header) }

    init {
        "project & unproject" {
            var deltaLongitudeMax = 0.0
            var deltaLatitudeMax = 0.0

            val counter = IntArray(3)

            for (a in -90..90) {
                for (b in 0..359) {
                    counter[0]++

                    try {
                        val latitude = a.deg
                        val longitude = b.deg

                        if (wcs.inside(longitude, latitude)) {
                            counter[1]++

                            val pix = wcs.worldToPixel(longitude, latitude)
                            val sky = wcs.pixelToWorld(pix[0], pix[1])

                            var deltaLongitude = abs(sky.first.degrees - b)

                            if (deltaLongitude > 180) {
                                deltaLongitude = 360 - deltaLongitude
                            }

                            if (abs(a) != 90 && deltaLongitude > deltaLongitudeMax) {
                                deltaLongitudeMax = deltaLongitude
                            }

                            val deltaLatitude = abs(sky.second.degrees - a)

                            if (deltaLatitude > deltaLatitudeMax) {
                                deltaLatitudeMax = deltaLatitude
                            }

                            if (deltaLatitude > 1e-11) {
                                println("lng=${longitude.degrees} lat=${latitude.degrees} | project: x=${pix[0]}, y=${pix[1]} | unproject: lng=${sky.first.degrees}, lat=${sky.second.degrees}")
                            } else if (abs(a) != 90 && deltaLongitude > 1e-11) {
                                println("lng=${longitude.degrees} lat=${latitude.degrees} | project: x=${pix[0]}, y=${pix[1]} | unproject: lng=${sky.first.degrees}, lat=${sky.second.degrees}")
                            }
                        } else {
                            counter[2]++
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }

            println("Counters: total: ${counter[0]}, inside: ${counter[1]}, outside: ${counter[2]}")
            println("Maximum residual (sky): lng: $deltaLongitudeMax,  lat: $deltaLatitudeMax")

            deltaLongitudeMax shouldBe (1e-12 plusOrMinus 1e-10)
            deltaLatitudeMax shouldBe (1e-12 plusOrMinus 1e-10)
        }
    }
}
