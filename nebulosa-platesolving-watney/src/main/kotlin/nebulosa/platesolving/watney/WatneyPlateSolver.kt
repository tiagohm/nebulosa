package nebulosa.platesolving.watney

import nebulosa.math.Angle
import nebulosa.platesolving.Calibration
import nebulosa.platesolving.PlateSolver
import nebulosa.platesolving.PlateSolvingException
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.io.path.bufferedReader
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.math.ceil

/**
 * @see <a href="https://github.com/Jusas/WatneyAstrometry">GitHub</a>
 */
class WatneyPlateSolver(private val path: String) : PlateSolver {

    override fun solve(
        file: File,
        blind: Boolean,
        centerRA: Angle,
        centerDEC: Angle,
        radius: Angle,
        downsampleFactor: Int,
        timeout: Duration?
    ): Calibration {
        val args = arrayListOf<String>()

        args.add(path)

        args.add(if (blind) "blind" else "nearby")

        val outFile = Files.createTempFile("watney", ".json")
        args.add("--out")
        args.add("$outFile")

        if (!blind) {
            args.add("--manual")

            args.add("--ra")
            args.add("${centerRA.degrees}")

            args.add("--dec")
            args.add("${centerDEC.degrees}")

            args.add("--field-radius")
            args.add("${ceil(radius.degrees)}")
        } else {
            args.add("--min-radius")
            args.add("0.25")

            args.add("--max-radius")
            args.add("8")
        }

        args.add("--image")
        args.add("$file")

        args.add("--out-format")
        args.add("tsv")

        args.add("--sampling")
        args.add("8")

        args.add("--extended")

        LOG.info("local solving. command={}", args)

        val process = ProcessBuilder(args)
            .start()

        try {
            process.waitFor(timeout?.seconds ?: 300, TimeUnit.SECONDS)

            LOG.info("watney exited. code={}", process.exitValue())

            val parameters = HashMap<String, String>(32)

            if (outFile.exists()) {
                for (line in outFile.bufferedReader().lines()) {
                    val parts = line.split('\t')
                    if (parts.size > 1) parameters[parts[0]] = parts[1]
                }
            }

            LOG.info("watney solution. parameters={}", parameters)

            val solved = parameters["success"] == "true"

            if (solved) {
                val ctype1 = "RA---TAN"
                val ctype2 = "DEC--TAN"
                val crpix1 = parameters["fits_crpix1"]!!.toDouble()
                val crpix2 = parameters["fits_crpix2"]!!.toDouble()
                val crval1 = Angle.from(parameters["fits_crval1"])!!
                val crval2 = Angle.from(parameters["fits_crval2"])!!
                val cdelt1 = Angle.from(parameters["fits_cdelt1"])!!
                val cdelt2 = Angle.from(parameters["fits_cdelt2"])!!
                val crota1 = Angle.from(parameters["fits_crota1"])!!
                val crota2 = Angle.from(parameters["fits_crota2"])!!
                val cd11 = parameters["fits_cd1_1"]!!.toDouble()
                val cd12 = parameters["fits_cd1_2"]!!.toDouble()
                val cd21 = parameters["fits_cd2_1"]!!.toDouble()
                val cd22 = parameters["fits_cd2_2"]!!.toDouble()

                val width = Angle.from(parameters["fieldWidth"])!!
                val height = Angle.from(parameters["fieldHeight"])!!

                val calibration = Calibration(
                    true,
                    ctype1, ctype2, crpix1, crpix2,
                    crval1, crval2, cdelt1, cdelt2, crota1, crota2,
                    cd11, cd12, cd21, cd22,
                    width = width, height = height,
                )

                LOG.info("astap solved. calibration={}", calibration)

                return calibration
            } else {
                throw PlateSolvingException("Plate solving failed.")
            }
        } catch (e: InterruptedException) {
            process.destroyForcibly()
        } finally {
            outFile.deleteIfExists()
        }

        return Calibration.EMPTY
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(WatneyPlateSolver::class.java)
    }
}
