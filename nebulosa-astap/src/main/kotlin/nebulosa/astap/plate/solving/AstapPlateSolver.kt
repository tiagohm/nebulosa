package nebulosa.astap.plate.solving

import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.common.process.ProcessExecutor
import nebulosa.fits.FitsHeader
import nebulosa.fits.FitsKeywordDictionary
import nebulosa.image.Image
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
        path: Path?, image: Image?,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration?,
        cancellationToken: CancellationToken,
    ): PlateSolution {
        requireNotNull(path) { "path is required" }

        val arguments = mutableMapOf<String, Any?>()

        val basePath = Files.createTempDirectory("astap")
        val baseName = UUID.randomUUID().toString()
        val outFile = Paths.get("$basePath", baseName)

        arguments["-o"] = outFile
        arguments["-z"] = downsampleFactor
        arguments["-fov"] = 0 // auto

        if (radius.toDegrees >= 0.1 && centerRA.isFinite() && centerDEC.isFinite()) {
            arguments["-ra"] = centerRA.toHours
            arguments["-spd"] = centerDEC.toDegrees + 90.0
            arguments["-r"] = ceil(radius.toDegrees)
        } else {
            arguments["-r"] = "180.0"
        }

        arguments["-f"] = path

        LOG.info("ASTAP solving. command={}", arguments)

        try {
            val timeoutOrDefault = timeout?.takeIf { it.toSeconds() > 0 } ?: Duration.ofMinutes(5)
            val process = executor.execute(arguments, timeoutOrDefault, path.parent, cancellationToken)

            if (process.isAlive) process.destroyForcibly()
            LOG.info("astap exited. code={}", process.exitValue())

            if (cancellationToken.isCancelled) return PlateSolution.NO_SOLUTION

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

                val header = FitsHeader()
                header.add(FitsKeywordDictionary.CTYPE1, ctype1)
                header.add(FitsKeywordDictionary.CTYPE2, ctype2)
                header.add(FitsKeywordDictionary.CRPIX1, crpix1)
                header.add(FitsKeywordDictionary.CRPIX2, crpix2)
                header.add(FitsKeywordDictionary.CRVAL1, crval1)
                header.add(FitsKeywordDictionary.CRVAL2, crval2)
                header.add(FitsKeywordDictionary.CDELT1, cdelt1)
                header.add(FitsKeywordDictionary.CDELT2, cdelt2)
                header.add(FitsKeywordDictionary.CROTA1, crota1)
                header.add(FitsKeywordDictionary.CROTA2, crota2)
                header.add(FitsKeywordDictionary.CD1_1, cd11)
                header.add(FitsKeywordDictionary.CD1_2, cd12)
                header.add(FitsKeywordDictionary.CD2_1, cd21)
                header.add(FitsKeywordDictionary.CD2_2, cd22)

                val solution = PlateSolution(true, crota2.deg, cdelt2.deg, crval1.deg, crval2.deg, width.deg, height.deg, header = header)

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
