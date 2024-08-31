package nebulosa.siril.command

import nebulosa.fits.FitsHeader
import nebulosa.fits.FitsHeaderCard
import nebulosa.image.format.Header
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.util.concurrency.latch.CountUpDownLatch
import nebulosa.util.exec.CommandLineListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

data class DumpHeader(private val header: Header = FitsHeader()) : SirilCommand<Header>, CommandLineListener {

    private val latch = CountUpDownLatch(1)
    private val started = AtomicBoolean()
    private val finished = AtomicBoolean()

    override fun onLineRead(line: String) {
        if (finished.get()) return

        if (started.get()) {
            val card = line.replaceFirst("log: ", "").trimStart()

            try {
                with(FitsHeaderCard.from(card)) {
                    header.add(this)

                    LOG.debug { line }

                    if (key == "END") {
                        finished.set(true)
                        latch.reset()
                    }
                }
            } catch (ignored: Throwable) {
            }
        } else if (line.contains("FITS header for currently loaded image", true)) {
            started.set(true)
        }
    }

    override fun write(commandLine: SirilCommandLine): Header {
        return try {
            commandLine.registerCommandLineListener(this)
            commandLine.write("dumpheader")
            latch.await(15, TimeUnit.SECONDS)
            header
        } finally {
            commandLine.unregisterCommandLineListener(this)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<DumpHeader>()
    }
}
