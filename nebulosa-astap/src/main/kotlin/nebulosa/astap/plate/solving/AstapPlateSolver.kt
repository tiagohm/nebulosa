package nebulosa.astap.plate.solving

import nebulosa.common.process.ProcessExecutor
import nebulosa.fits.NOAOExt
import nebulosa.fits.Standard
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.toDegrees
import nebulosa.math.toHours
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolver
import nebulosa.plate.solving.PlateSolvingException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.util.*
import kotlin.io.path.deleteRecursively
import kotlin.io.path.inputStream
import kotlin.math.ceil

/**
 * @see <a href="https://www.hnsky.org/astap.htm#astap_command_line">README</a>
 */
class AstapPlateSolver(path: Path) : PlateSolver {

    private val executor = ProcessExecutor(path)

    override fun solve(
        path: Path,
        blind: Boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration?,
    ): PlateSolution {
        val arguments = mutableMapOf<String, Any?>()

        val basePath = Files.createTempDirectory("astap")
        val baseName = UUID.randomUUID().toString()
        val outFile = Paths.get("$basePath", baseName)

        arguments["-o"] = outFile
        arguments["-z"] = downsampleFactor

        if (!blind) {
            arguments["-ra"] = centerRA.toHours
            arguments["-spd"] = centerDEC.toDegrees + 90.0
            arguments["-r"] = ceil(radius.toDegrees)
        } else {
            arguments["-r"] = "180.0"
        }

        arguments["-f"] = path

        LOG.info("local solving. command={}", arguments)

        try {
            val process = executor.execute(arguments, timeout ?: Duration.ofSeconds(300), path.parent)

            LOG.info("astap exited. code={}", process.exitValue())

            val ini = Properties()
            Paths.get("$basePath", "$baseName.ini").inputStream().use(ini::load)

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

                val solution = PlateSolution(true, crota2.deg, cdelt2.deg, crval1.deg, crval2.deg, width.deg, height.deg)

                solution.add(Standard.CTYPE1, ctype1)
                solution.add(Standard.CTYPE2, ctype2)
                solution.add(Standard.CRPIX1, crpix1)
                solution.add(Standard.CRPIX2, crpix2)
                solution.add(Standard.CRVAL1, crval1)
                solution.add(Standard.CRVAL2, crval2)
                solution.add(Standard.CDELT1, cdelt1)
                solution.add(Standard.CDELT2, cdelt2)
                solution.add(Standard.CROTA1, crota1)
                solution.add(Standard.CROTA2, crota2)
                solution.add(NOAOExt.CD1_1, cd11)
                solution.add(NOAOExt.CD1_2, cd12)
                solution.add(NOAOExt.CD2_1, cd21)
                solution.add(NOAOExt.CD2_2, cd22)

                LOG.info("astap solved. calibration={}", solution)

                return solution
            } else {
                val message = ini.getProperty("ERROR")
                    ?: ini.getProperty("WARNING")
                    ?: "plate solving failed"
                throw PlateSolvingException(message)
            }
        } finally {
            basePath.deleteRecursively()
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<AstapPlateSolver>()
    }
}
