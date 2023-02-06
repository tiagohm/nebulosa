import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.math.Distance.Companion.km
import nebulosa.math.Velocity.Companion.kms
import nebulosa.nasa.daf.RemoteDaf
import nebulosa.nasa.spk.Spk
import nebulosa.nova.astrometry.SpiceKernel
import nebulosa.nova.position.Barycentric
import nebulosa.nova.position.ICRF
import nebulosa.time.TDB
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
        // Start time      : A.D. 2022-Nov-27 22:30:00.0000 TDB
        // Stop  time      : A.D. 2022-Nov-27 22:30:00.0000 TDB
        // Step-size       : DISCRETE TIME-LIST
        // *******************************************************************************
        "Mars Barycenter -> Mars" {
            val mars = kernel[499] - kernel[4]
            val icrf = mars.at<ICRF>(TDB(TimeYMDHMS(2022, 11, 27, 22, 30, 0.0)))
            icrf.position[0] shouldBe (-6.973204561568924E-13 plusOrMinus 1e-13)
            icrf.position[1] shouldBe (-1.004168120568349E-12 plusOrMinus 1e-13)
            icrf.position[2] shouldBe (2.893822598829994E-13 plusOrMinus 1e-13)
            icrf.velocity[0] shouldBe (1.711398724127463E-11 plusOrMinus 1e-17)
            icrf.velocity[1] shouldBe (-9.753150151409278E-12 plusOrMinus 1e-16)
            icrf.velocity[2] shouldBe (-9.108512802684569E-12 plusOrMinus 1e-17)
        }
        // *******************************************************************************
        // Ephemeris / WWW_USER Mon Feb  6 10:40:58 2023 Pasadena, USA      / Horizons
        // *******************************************************************************
        // Target body name: Mars (499)                      {source: mar097}
        // Center body name: Solar System Barycenter (0)     {source: DE441}
        // Center-site name: BODY CENTER
        // *******************************************************************************
        // Start time      : A.D. 2022-Nov-27 22:30:00.0000 TDB
        // Stop  time      : A.D. 2022-Nov-27 22:40:00.0000 TDB
        // Step-size       : 1 minutes
        // *******************************************************************************
        "position of mars" {
            val mars = kernel[499]
            val barycentric = mars.at<Barycentric>(TDB(TimeYMDHMS(2022, 11, 27, 22, 30, 0.0)))
            barycentric.position[0] shouldBe (5.001501349246619E-01 plusOrMinus 1e-8)
            barycentric.position[1] shouldBe (1.433531335945670E+00 plusOrMinus 1e-8)
            barycentric.position[2] shouldBe (1.775276728211534E-02 plusOrMinus 1e-8)
            barycentric.velocity[0] shouldBe (-1.265507359060806E-02 plusOrMinus 1e-8)
            barycentric.velocity[1] shouldBe (5.867037891695070E-03 plusOrMinus 1e-8)
            barycentric.velocity[2] shouldBe (4.336514005374472E-04 plusOrMinus 1e-8)
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
