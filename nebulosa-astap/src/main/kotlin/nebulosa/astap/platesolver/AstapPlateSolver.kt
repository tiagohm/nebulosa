package nebulosa.astap.platesolver

import nebulosa.fits.FitsHeader
import nebulosa.fits.FitsKeyword
import nebulosa.image.Image
import nebulosa.log.di
import nebulosa.log.i
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.toDegrees
import nebulosa.math.toHours
import nebulosa.platesolver.PlateSolution
import nebulosa.platesolver.PlateSolver
import nebulosa.platesolver.PlateSolverException
import nebulosa.util.concurrency.cancellation.CancellationToken
import nebulosa.util.exec.commandLine
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
data class AstapPlateSolver(private val executablePath: Path) : PlateSolver {

    override fun solve(
        path: Path?, image: Image?,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration,
        cancellationToken: CancellationToken,
    ): PlateSolution {
        requireNotNull(path) { "path is required" }

        val basePath = Files.createTempDirectory("astap")
        val baseName = UUID.randomUUID().toString()
        val outFile = Paths.get("$basePath", baseName)

        val cmd = commandLine {
            executablePath(executablePath)
            workingDirectory(path.parent)

            putArg("-o", outFile)
            putArg("-z", downsampleFactor)
            putArg("-fov", "0") // auto

            if (radius.toDegrees >= 0.1 && centerRA.isFinite() && centerDEC.isFinite()) {
                putArg("-ra", centerRA.toHours)
                putArg("-spd", centerDEC.toDegrees + 90.0)
                putArg("-r", ceil(radius.toDegrees))
            } else {
                putArg("-r", "180.0")
            }

            putArg("-f", path)
        }

        LOG.di("astap solving. command={}", cmd.command)

        try {
            val timeoutOrDefault = timeout.takeIf { it.toSeconds() > 0 } ?: Duration.ofMinutes(5)
            cancellationToken.listen(cmd)
            cmd.start(timeoutOrDefault)

            LOG.di("astap exited. code={}", cmd.get())

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
                val widthInPixels = dimensions[0].trim().toDouble()
                val heightInPixels = dimensions[1].trim().toDouble()
                val width = cdelt1 * widthInPixels
                val height = cdelt2 * heightInPixels

                val header = FitsHeader()
                header.add(FitsKeyword.CTYPE1, ctype1)
                header.add(FitsKeyword.CTYPE2, ctype2)
                header.add(FitsKeyword.CRPIX1, crpix1)
                header.add(FitsKeyword.CRPIX2, crpix2)
                header.add(FitsKeyword.CRVAL1, crval1)
                header.add(FitsKeyword.CRVAL2, crval2)
                header.add(FitsKeyword.CDELT1, cdelt1)
                header.add(FitsKeyword.CDELT2, cdelt2)
                header.add(FitsKeyword.CROTA1, crota1)
                header.add(FitsKeyword.CROTA2, crota2)
                header.add(FitsKeyword.CD1_1, cd11)
                header.add(FitsKeyword.CD1_2, cd12)
                header.add(FitsKeyword.CD2_1, cd21)
                header.add(FitsKeyword.CD2_2, cd22)

                val solution = PlateSolution(
                    true, crota2.deg, cdelt2.deg, crval1.deg, crval2.deg, width.deg, height.deg,
                    widthInPixels = widthInPixels, heightInPixels = heightInPixels, header = header
                )

                LOG.i("astap solved. calibration={}", solution)

                return solution
            } else {
                val message = ini.getProperty("ERROR")
                    ?: ini.getProperty("WARNING")
                    ?: "plate solving failed"
                throw PlateSolverException(message)
            }
        } finally {
            cancellationToken.unlisten(cmd)
            basePath.deleteRecursively()
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<AstapPlateSolver>()
    }
}
