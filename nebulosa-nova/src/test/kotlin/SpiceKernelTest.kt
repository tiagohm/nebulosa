import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.math.Distance.Companion.km
import nebulosa.math.Velocity.Companion.kms
import nebulosa.nasa.daf.RemoteDaf
import nebulosa.nasa.spk.Spk
import nebulosa.nova.position.ICRF
import nebulosa.nova.astrometry.SpiceKernel
import nebulosa.nova.position.Barycentric
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC

class SpiceKernelTest : StringSpec() {

    init {
        val time = UTC(TimeYMDHMS(2022, 11, 27, 22, 30, 0.0))

        val de441 = Spk(RemoteDaf("https://ssd.jpl.nasa.gov/ftp/eph/planets/bsp/de441.bsp"))
        val mar097 = Spk(RemoteDaf("https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/mar097.bsp"))
        val kernel = SpiceKernel(de441, mar097)

        // *******************************************************************************
        // Ephemeris / WWW_USER Thu Dec  8 10:51:14 2022 Pasadena, USA      / Horizons
        // *******************************************************************************
        // Target body name: Mars Barycenter (4)             {source: DE441}
        // Center body name: Solar System Barycenter (0)     {source: DE441}
        // Center-site name: BODY CENTER
        // *******************************************************************************
        // Start time      : A.D. 2022-Nov-27 22:30:00.0000 UT
        // Stop  time      : A.D. 2022-Nov-27 22:30:00.0000 UT
        // Step-size       : DISCRETE TIME-LIST
        // *******************************************************************************
        "SSB -> Mars barycenter" {
            val mars = kernel[4]
            val barycentric = mars.at<Barycentric>(time)
            barycentric.position[0] shouldBe (7.481977318479481E+07.km.value plusOrMinus 1e-4)
            barycentric.position[1] shouldBe (1.957011669298882E+08.km.value plusOrMinus 1e-4)
            barycentric.position[2] shouldBe (8.774154566650333E+07.km.value plusOrMinus 1e-4)
            barycentric.velocity[0] shouldBe (-2.191177306531380E+01.kms.value plusOrMinus 1e-9)
            barycentric.velocity[1] shouldBe (9.021439569712342E+00.kms.value plusOrMinus 1e-9)
            barycentric.velocity[2] shouldBe (4.729650205452117E+00.kms.value plusOrMinus 1e-9)
        }
        // *******************************************************************************
        // Ephemeris / WWW_USER Thu Dec  8 10:44:16 2022 Pasadena, USA      / Horizons
        // *******************************************************************************
        // Target body name: Mars (499)                      {source: mar097}
        // Center body name: Mars Barycenter (4)             {source: mar097}
        // Center-site name: BODY CENTER
        // *******************************************************************************
        // Start time      : A.D. 2022-Nov-27 22:30:00.0000 UT
        // Stop  time      : A.D. 2022-Nov-27 22:30:00.0000 UT
        // Step-size       : DISCRETE TIME-LIST
        // *******************************************************************************
        "Mars Barycenter -> Mars" {
            val mars = kernel[499] - kernel[4]
            val barycentric = mars.at<ICRF>(time)
            barycentric.position[0] shouldBe (-1.022599607532902E-04.km.value plusOrMinus 1e-13)
            barycentric.position[1] shouldBe (-1.556656127725825E-04.km.value plusOrMinus 1e-13)
            barycentric.position[2] shouldBe (-2.149732620186122E-05.km.value plusOrMinus 1e-13)
            barycentric.velocity[0] shouldBe (2.985220321993323E-08.kms.value plusOrMinus 1e-17)
            barycentric.velocity[1] shouldBe (-8.702897222485961E-09.kms.value plusOrMinus 1e-16)
            barycentric.velocity[2] shouldBe (-2.105867599542233E-08.kms.value plusOrMinus 1e-17)
        }
        // *******************************************************************************
        // Ephemeris / WWW_USER Thu Dec  8 11:01:30 2022 Pasadena, USA      / Horizons
        // *******************************************************************************
        // Target body name: Mars (499)                      {source: mar097}
        // Center body name: Solar System Barycenter (0)     {source: DE441}
        // Center-site name: BODY CENTER
        // *******************************************************************************
        // Start time      : A.D. 2022-Nov-27 22:30:00.0000 UT
        // Stop  time      : A.D. 2022-Nov-27 22:30:00.0000 UT
        // Step-size       : DISCRETE TIME-LIST
        // *******************************************************************************
        "position of mars" {
            val mars = kernel[499]
            val barycentric = mars.at<Barycentric>(time)
            barycentric.position[0] shouldBe (7.481987929541600E+07.km.value plusOrMinus 5e-1)
            barycentric.position[1] shouldBe (1.957012137060448E+08.km.value plusOrMinus 5e-1)
            barycentric.position[2] shouldBe (2.271465592592384E+08.km.value plusOrMinus 5e-1)
            barycentric.velocity[0] shouldBe (-2.191177303913402E+01.kms.value plusOrMinus 5e-1)
            barycentric.velocity[1] shouldBe (9.021439562722357E+00.kms.value plusOrMinus 5e-1)
            barycentric.velocity[2] shouldBe (4.729650188195660E+00.kms.value plusOrMinus 5e-1)
        }
        // *******************************************************************************
        // Ephemeris / WWW_USER Thu Dec  8 11:22:32 2022 Pasadena, USA      / Horizons
        // *******************************************************************************
        // Target body name: Mars (499)                      {source: mar097}
        // Center body name: Earth (399)                     {source: mar097}
        // Center-site name: BODY CENTER
        // *******************************************************************************
        // Start time      : A.D. 2022-Nov-27 22:30:00.0000 UT
        // Stop  time      : A.D. 2022-Nov-27 22:30:00.0000 UT
        // Step-size       : DISCRETE TIME-LIST
        // *******************************************************************************
        "position of mars viewed from earth" {
            val earth = kernel[399]
            val mars = kernel[499]
            val barycentric = earth.at<Barycentric>(time)
            val astrometric = barycentric.observe(mars)
            astrometric.position[0] shouldBe (1.459761119093829E+07.km.value plusOrMinus 1e-7)
            astrometric.position[1] shouldBe (7.257056846567649E+07.km.value plusOrMinus 1e-7)
            astrometric.position[2] shouldBe (3.433143337250027E+07.km.value plusOrMinus 1e-7)
            astrometric.velocity[0] shouldBe (5.654922729629408E+00.kms.value plusOrMinus 1e-7)
            astrometric.velocity[1] shouldBe (-2.257292357112751E+00.kms.value plusOrMinus 1e-7)
            astrometric.velocity[2] shouldBe (-1.601801539501597E-01.kms.value plusOrMinus 1e-7)
        }
    }
}
