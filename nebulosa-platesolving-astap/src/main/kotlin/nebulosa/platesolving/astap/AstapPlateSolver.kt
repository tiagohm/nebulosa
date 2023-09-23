package nebulosa.platesolving.astap

import nebulosa.fits.FitsKeywords
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.platesolving.Calibration
import nebulosa.platesolving.PlateSolver
import nebulosa.platesolving.PlateSolvingException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.deleteRecursively
import kotlin.io.path.inputStream
import kotlin.math.ceil

/**
 * @see <a href="https://www.hnsky.org/astap.htm#astap_command_line">README</a>
 */
class AstapPlateSolver(private val solverPath: String) : PlateSolver {

    override fun solve(
        path: Path,
        blind: Boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int,
        timeout: Duration?,
    ): Calibration {
        val args = arrayListOf<String>()

        args.add(solverPath)

        val basePath = Files.createTempDirectory("astap")
        val baseName = UUID.randomUUID().toString()
        val outFile = Paths.get("$basePath", baseName)
        args.add("-o")
        args.add("$outFile")

        args.add("-z")
        args.add("$downsampleFactor")

        if (!blind) {
            args.add("-ra")
            args.add("${centerRA.hours}")

            args.add("-spd")
            args.add("${centerDEC.degrees + 90.0}")

            args.add("-r")
            args.add("${ceil(radius.degrees)}")
        } else {
            args.add("-r")
            args.add("180.0")
        }

        args.add("-f")
        args.add("$path")

        LOG.info("local solving. command={}", args)

        val process = ProcessBuilder(args)
            .start()

        try {
            process.waitFor(timeout?.seconds ?: 300, TimeUnit.SECONDS)

            LOG.info("astap exited. code={}", process.exitValue())

            val iniFile = Paths.get("$basePath", "$baseName.ini")
            val ini = Properties()
            iniFile.inputStream().use(ini::load)

            val solved = ini.getProperty("PLTSOLVD").trim() == "T"

            if (solved) {
                val ctype1 = ini.getProperty("CTYPE1", "RA---TAN")
                val ctype2 = ini.getProperty("CTYPE2", "DEC--TAN")
                val crpix1 = ini.getProperty("CRPIX1").toDouble()
                val crpix2 = ini.getProperty("CRPIX2").toDouble()
                val crval1 = ini.getProperty("CRVAL1").toDouble()
                val crval2 = ini.getProperty("CRVAL2").toDouble()
                val cdelt1 = ini.getProperty("CDELT1").toDouble()
                val cdelt2 = ini.getProperty("CDELT2").toDouble()
                val crota1 = ini.getProperty("CROTA1").toDouble()
                val crota2 = ini.getProperty("CROTA2").toDouble()
                val cd11 = ini.getProperty("CD1_1").toDouble()
                val cd12 = ini.getProperty("CD1_2").toDouble()
                val cd21 = ini.getProperty("CD2_1").toDouble()
                val cd22 = ini.getProperty("CD2_2").toDouble()

                val dimensions = ini.getProperty("DIMENSIONS").split("x")
                val width = cdelt1 * dimensions[0].trim().toDouble()
                val height = cdelt2 * dimensions[1].trim().toDouble()

                val calibration = Calibration(true, crota2.deg, cdelt2.deg, crval1.deg, crval2.deg, width.deg, height.deg)

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

                LOG.info("astap solved. calibration={}", calibration)

                return calibration
            } else {
                val message = ini.getProperty("ERROR")
                    ?: ini.getProperty("WARNING")
                    ?: "plate solving failed"
                throw PlateSolvingException(message)
            }
        } catch (e: InterruptedException) {
            process.destroyForcibly()
        } finally {
            basePath.deleteRecursively()
        }

        return Calibration()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<AstapPlateSolver>()
    }
}
