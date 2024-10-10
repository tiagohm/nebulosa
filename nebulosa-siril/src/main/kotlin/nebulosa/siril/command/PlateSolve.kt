package nebulosa.siril.command

import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.math.*
import nebulosa.platesolver.Parity
import nebulosa.platesolver.PlateSolution
import nebulosa.util.concurrency.latch.CountUpDownLatch
import nebulosa.util.exec.CommandLineListener
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Plate solves the loaded image.
 *
 * ```txt
 * log: Up is +164.28 deg ClockWise wrt. N
 * log: Resolution:      1.368 arcsec/px
 * log: Focal length: 1395.85 mm
 * log: Pixel size:      9.26 µm
 * log: Field of view:    47' 15.23" x 32' 9.38"
 * log: Saved focal length 1395.85 and pixel size 9.26 as default values
 * log: Image center: alpha: 13h25m38s, delta: -43°00'52"
 * ```
 */
data class PlateSolve(
    @JvmField val path: Path,
    @JvmField val focalLength: Double = 0.0,
    @JvmField val pixelSize: Double = 0.0,
    @JvmField val useCenterCoordinates: Boolean = false,
    @JvmField val rightAscension: Angle = 0.0, val declination: Angle = 0.0,
    @JvmField val downsampleFactor: Int = 0,
    @JvmField val timeout: Duration = Duration.ZERO,
) : SirilCommand<PlateSolution>, CommandLineListener {

    private val command by lazy {
        buildString(256) {
            append("platesolve")
            if (useCenterCoordinates) append(" ${rightAscension.toHours},${declination.toDegrees}")
            append(" -platesolve -noflip")
            if (focalLength > 0.0) append(" -focal=$focalLength")
            if (pixelSize > 0.0) append(" -pixelsize=$pixelSize")
            if (downsampleFactor > 1) append(" -downscale")
        }
    }

    private val latch = CountUpDownLatch(1)
    private val success = AtomicBoolean()
    private val exited = AtomicBoolean()

    @Volatile private var orientation: Angle = 0.0
    @Volatile private var parity = Parity.NORMAL
    @Volatile private var resolution: Angle = 0.0
    @Volatile private var fovWidth: Angle = 0.0
    @Volatile private var fovHeight: Angle = 0.0
    @Volatile private var imageCenterRA: Angle = 0.0
    @Volatile private var imageCenterDEC: Angle = 0.0

    override fun onLineRead(line: String) {
        LOG.debug { line }

        if (line.matchesOrientation() || line.matchesUndeterminatedOrientation() ||
            line.matchesResolution() || line.matchesFOV()
        ) {
            return
        }

        if (line.matchesImageCenter()) {
            success.set(true)
            latch.reset()
        }
    }

    override fun onExit(exitCode: Int, exception: Throwable?) {
        LOG.info("plate solver finished. exitCode={}", exitCode, exception)
        exited.set(true)
        latch.reset()
    }

    override fun write(commandLine: SirilCommandLine): PlateSolution {
        if (commandLine.execute(Load(path))) {
            LOG.info("plate solver started. pid={}", commandLine.pid)

            try {
                commandLine.registerCommandLineListener(this)
                commandLine.write(command)

                if (!latch.await(maxOf(timeout, MIN_TIMEOUT)) || exited.get() || !success.get()) {
                    return PlateSolution.NO_SOLUTION
                }
            } finally {
                commandLine.unregisterCommandLineListener(this)
            }

            if (success.get()) {
                return PlateSolution(true, orientation, resolution, imageCenterRA, imageCenterDEC, fovWidth, fovHeight, parity)
            }
        } else {
            LOG.error("failed to load $path")
        }

        return PlateSolution.NO_SOLUTION
    }

    private fun String.matchesOrientation(): Boolean {
        val m = ORIENTATION_REGEX.find(this) ?: return false
        orientation = m.groupValues[1].toDouble().deg
        parity = if ("flipped" in this) Parity.FLIPPED else Parity.NORMAL
        return true
    }

    private fun String.matchesUndeterminatedOrientation(): Boolean {
        return if (startsWith(UNDETERMINED_ORIENTATION_REGEX)) {
            orientation = 0.0
            true
        } else {
            false
        }
    }

    private fun String.matchesResolution(): Boolean {
        val m = RESOLUTION_REGEX.find(this) ?: return false
        resolution = m.groupValues[1].toDouble().arcsec
        return true
    }

    private fun String.matchesFOV(): Boolean {
        val m = FOV_REGEX.find(this) ?: return false
        val (_, width, height) = m.groupValues
        fovWidth = width.parseFovInDHMS()
        fovHeight = height.parseFovInDHMS()
        return true
    }

    private fun String.matchesImageCenter(): Boolean {
        val m = IMAGE_CENTER_REGEX.find(this) ?: return false
        val (_, alpha, delta) = m.groupValues
        imageCenterRA = alpha.hours
        imageCenterDEC = delta.deg
        return true
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<PlateSolve>()
        @JvmStatic private val MIN_TIMEOUT = Duration.ofSeconds(30)

        private const val INT_REGEX = "\\d+"
        private const val SIGNED_INT_REGEX = "[-+]?$INT_REGEX"
        private const val FLOAT_REGEX = "$SIGNED_INT_REGEX(?:\\.$INT_REGEX)?"

        // https://gitlab.com/free-astro/siril/-/blob/master/src/algos/astrometry_solver.c

        @JvmStatic private val ORIENTATION_REGEX = "log: Up is ($FLOAT_REGEX) deg ClockWise wrt. N( \\(flipped\\))?".toRegex()
        private const val UNDETERMINED_ORIENTATION_REGEX = "log: Up position wrt. N is undetermined"
        @JvmStatic private val RESOLUTION_REGEX = "log: Resolution:\\s*($FLOAT_REGEX) arcsec/px".toRegex()
        @JvmStatic private val FOV_REGEX = "log: Field of view:\\s*(.*) x (.*)".toRegex()
        @JvmStatic private val IMAGE_CENTER_REGEX = "log: Image center: alpha: (.*), delta: (.*)".toRegex()

        @JvmStatic
        private fun String.parseFovInDHMS(): Angle {
            val parts = split(" ")

            var value = 0.0

            for (part in parts) {
                if (part.endsWith('d')) {
                    value = part.substring(0, part.length - 1).toDouble()
                } else if (part.endsWith('m') || part.endsWith('\'')) {
                    value += part.substring(0, part.length - 1).toDouble() / 60.0
                } else if (part.endsWith('s') || part.endsWith('"')) {
                    value += part.substring(0, part.length - 1).toDouble() / 3600.0
                }
            }

            return value.deg
        }
    }
}
