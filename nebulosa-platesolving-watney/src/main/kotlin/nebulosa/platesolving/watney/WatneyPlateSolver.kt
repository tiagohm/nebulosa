package nebulosa.platesolving.watney

import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.platesolving.Calibration
import nebulosa.platesolving.PlateSolver
import nebulosa.platesolving.PlateSolvingException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.io.path.bufferedReader
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.math.ceil

/**
 * @see <a href="https://github.com/Jusas/WatneyAstrometry">GitHub</a>
 */
class WatneyPlateSolver(private val solverPath: String) : PlateSolver {

    override fun solve(
        path: Path,
        blind: Boolean,
        centerRA: Angle,
        centerDEC: Angle,
        radius: Angle,
        downsampleFactor: Int,
        timeout: Duration?,
    ): Calibration {
        val args = arrayListOf<String>()

        args.add(solverPath)

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
        args.add("$path")

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
                val crval1 = parameters["fits_crval1"].deg
                val crval2 = parameters["fits_crval2"].deg
                val cdelt1 = parameters["fits_cdelt1"].deg
                val cdelt2 = parameters["fits_cdelt2"].deg
                val crota1 = parameters["fits_crota1"].deg
                val crota2 = parameters["fits_crota2"].deg
                val cd11 = parameters["fits_cd1_1"]!!.toDouble()
                val cd12 = parameters["fits_cd1_2"]!!.toDouble()
                val cd21 = parameters["fits_cd2_1"]!!.toDouble()
                val cd22 = parameters["fits_cd2_2"]!!.toDouble()

                val width = parameters["fieldWidth"].deg
                val height = parameters["fieldHeight"].deg

                val calibration = Calibration(
                    true,
                    ctype1, ctype2, crpix1, crpix2,
                    crval1, crval2, cdelt1, cdelt2, crota1, crota2,
                    true, cd11, cd12, cd21, cd22,
                    width = width, height = height,
                )

                LOG.info("watney solved. calibration={}", calibration)

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

        @JvmStatic private val LOG = loggerFor<WatneyPlateSolver>()
    }
}
