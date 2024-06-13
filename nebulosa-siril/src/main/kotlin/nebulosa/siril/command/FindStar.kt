package nebulosa.siril.command

import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.common.exec.CommandLineListener
import nebulosa.fits.height
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.stardetector.StarPoint
import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.bufferedReader

/**
 * Detects stars in the currently loaded image.
 */
data class FindStar(
    @JvmField val path: Path,
    @JvmField val maxStars: Int = 0,
) : SirilCommand<List<FindStar.Star>>, CommandLineListener, Closeable {

    data class Star(
        override val x: Double,
        override val y: Double,
        override val hfd: Double,
        override val snr: Double,
        override val flux: Double,
    ) : StarPoint

    private val outputPath by lazy { Files.createTempFile("siril-", ".txt") }

    private val command by lazy {
        buildString(256) {
            append("findstar \"-out=$outputPath\"")
            if (maxStars > 0) append(" -maxstars=$maxStars")
        }
    }

    private val latch = CountUpDownLatch(0)

    override fun onLineRead(line: String) {
        LOG.debug { line }

        if (line.startsWith("log: The file") && line.endsWith("has been created.")) {
            latch.reset()
        }
    }

    override fun onExit(exitCode: Int, exception: Throwable?) {
        latch.reset()
    }

    override fun write(commandLine: SirilCommandLine): List<Star> {
        if (commandLine.execute(Load(path))) {
            try {
                latch.countUp()

                commandLine.registerCommandLineListener(this)
                commandLine.write(command)

                if (!latch.await(15, TimeUnit.SECONDS)) {
                    return emptyList()
                }
            } finally {
                commandLine.unregisterCommandLineListener(this)
                close()
            }

            val header = commandLine.execute(DumpHeader())
            Thread.sleep(1000)
            return outputPath.parseStars(header.height)
        } else {
            return emptyList()
        }
    }

    override fun close() {
        // outputPath.deleteIfExists()
    }

    companion object {

        const val FWHM = 1.1774100225154747 // sqrt(2 * ln(2))

        @JvmStatic private val LOG = loggerFor<FindStar>()

        @JvmStatic
        private fun Path.parseStars(height: Int): List<Star> {
            val stars = ArrayList<Star>(256)

            bufferedReader().use {
                for (line in it.lines()) {
                    if (line.startsWith('#')) continue

                    val columns = line.split('\t')
                    val flux = columns[3].trim().toDouble() // A ???
                    val x = columns[5].trim().toDouble() // X
                    val y = columns[6].trim().toDouble() // Y
                    val fwhmx = columns[7].trim().toDouble() // FWHMx [px]
                    val fwhmy = columns[8].trim().toDouble() // FWHMy [px]
                    val snr = columns[12].trim().toDouble() // RMSE ???
                    val fwhm = (fwhmx + fwhmy) / 2.0
                    val hfd = fwhm / FWHM

                    stars.add(Star(x, height - y, hfd, snr, flux))
                }
            }

            return stars
        }
    }
}
