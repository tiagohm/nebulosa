package nebulosa.platesolving.watney

import nebulosa.fits.FitsKeywords
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.toDegrees
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
            args.add("${centerRA.toDegrees}")

            args.add("--dec")
            args.add("${centerDEC.toDegrees}")

            args.add("--field-radius")
            args.add("${ceil(radius.toDegrees)}")
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
                val crval1 = parameters["fits_crval1"]!!.toDouble()
                val crval2 = parameters["fits_crval2"]!!.toDouble()
                val cdelt1 = parameters["fits_cdelt1"]!!.toDouble()
                val cdelt2 = parameters["fits_cdelt2"]!!.toDouble()
                val crota1 = parameters["fits_crota1"]!!.toDouble()
                val crota2 = parameters["fits_crota2"]!!.toDouble()
                val cd11 = parameters["fits_cd1_1"]!!.toDouble()
                val cd12 = parameters["fits_cd1_2"]!!.toDouble()
                val cd21 = parameters["fits_cd2_1"]!!.toDouble()
                val cd22 = parameters["fits_cd2_2"]!!.toDouble()

                val width = parameters["fieldWidth"].deg
                val height = parameters["fieldHeight"].deg

                val calibration = Calibration(true, crota2.deg, cdelt2.deg, crval1.deg, crval2.deg, width, height)

                calibration.addValue(FitsKeywords.CTYPE1, ctype1)
                calibration.addValue(FitsKeywords.CTYPE2, ctype2)
                calibration.addValue(FitsKeywords.CRPIX1, crpix1)
                calibration.addValue(FitsKeywords.CRPIX2, crpix2)
                calibration.addValue(FitsKeywords.CRVAL1, crval1)
                calibration.addValue(FitsKeywords.CRVAL2, crval2)
                calibration.addValue(FitsKeywords.CDELT1, cdelt1)
                calibration.addValue(FitsKeywords.CDELT2, cdelt2)
                calibration.addValue(FitsKeywords.CROTA1, crota1)
                calibration.addValue(FitsKeywords.CROTA2, crota2)
                calibration.addValue(FitsKeywords.CD1_1, cd11)
                calibration.addValue(FitsKeywords.CD1_2, cd12)
                calibration.addValue(FitsKeywords.CD2_1, cd21)
                calibration.addValue(FitsKeywords.CD2_2, cd22)

                LOG.info("watney solved. calibration={}", calibration)

                return calibration
            } else {
                throw PlateSolvingException("plate solving failed")
            }
        } catch (e: InterruptedException) {
            process.destroyForcibly()
        } finally {
            outFile.deleteIfExists()
        }

        return Calibration()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<WatneyPlateSolver>()
    }
}
