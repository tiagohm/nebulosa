package nebulosa.platesolving.astap

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.arcmin
import nebulosa.platesolving.Calibration
import nebulosa.platesolving.PlateSolver
import nebulosa.platesolving.PlateSolvingException
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.deleteRecursively
import kotlin.io.path.inputStream
import kotlin.math.hypot

/**
 * @see <a href="https://www.hnsky.org/astap.htm#astap_command_line">README</a>
 */
class AstapPlateSolver(private val path: String) : PlateSolver {

    override fun solve(
        file: File,
        blind: Boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int,
        timeout: Duration?,
    ): Calibration {
        val args = arrayListOf<String>()

        args.add(path)

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
            args.add("${radius.degrees}")
        } else {
            args.add("-r")
            args.add("180.0")
        }

        args.add("-f")
        args.add("$file")

        LOG.info("local solving. command={}", args)

        val process = ProcessBuilder(args)
            .start()

        try {
            process.waitFor(timeout?.seconds ?: 300, TimeUnit.SECONDS)

            LOG.info("astap exited. code={}", process.exitValue())

            val iniFile = Paths.get("$basePath", "$baseName.ini")
            val ini = Properties()
            iniFile.inputStream().use(ini::load)

            val solved = ini.getProperty("PLTSOLVD") == "T"

            if (solved) {
                val orientation = Angle.from(ini.getProperty("CROTA2"))!!
                val ra = Angle.from(ini.getProperty("CRVAL1"))!!
                val dec = Angle.from(ini.getProperty("CRVAL2"))!!
                val dimensions = ini.getProperty("DIMENSIONS").split("x")
                val scale = ini.getProperty("CDELT2")!!.toDouble() * 60.0 // arcmin
                val width = dimensions[0].trim().toInt() * scale
                val height = dimensions[1].trim().toInt() * scale
                val fieldRadius = hypot(width, height) / 2.0
                return Calibration(orientation, scale * 60.0, fieldRadius.arcmin, ra, dec, width, height)
            } else {
                val message = ini.getProperty("ERROR") ?: ini.getProperty("WARNING") ?: "Plate solving failed."
                throw PlateSolvingException(message)
            }
        } catch (e: InterruptedException) {
            process.destroyForcibly()
        } finally {
            basePath.deleteRecursively()
        }

        return Calibration.EMPTY
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(AstapPlateSolver::class.java)
    }
}
