package nebulosa.astap.stardetector

import de.siegmar.fastcsv.reader.CommentStrategy
import de.siegmar.fastcsv.reader.CsvReader
import nebulosa.log.d
import nebulosa.log.e
import nebulosa.log.loggerFor
import nebulosa.stardetector.StarDetector
import nebulosa.stardetector.StarPoint
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import java.io.InputStreamReader
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.nameWithoutExtension

data class AstapStarDetector(
    private val executablePath: Path,
    private val minSNR: Double = 0.0,
) : StarDetector<Path> {

    data class Star(
        override val x: Double = 0.0,
        override val y: Double = 0.0,
        override val hfd: Double = 0.0,
        override val snr: Double = 0.0,
        override val flux: Double = 0.0,
    ) : StarPoint

    override fun detect(input: Path): List<StarPoint> {
        val commandline = CommandLine.parse("$executablePath")
            .addArgument("-f").addArgument("$input")
            .addArgument("-z").addArgument("0")
            .addArgument("-extract").addArgument("$minSNR")

        try {
            val executor = DefaultExecutor.builder()
                .setWorkingDirectory(input.parent.toFile())
                .get()

            LOG.d("astap exited. code={}", executor.execute(commandline))
        } catch (e: Throwable) {
            LOG.e("astap failed", e)
            return emptyList()
        }

        val csvPath = Path.of("${input.parent}", "${input.nameWithoutExtension}.csv")

        if (!csvPath.exists()) return emptyList()

        val detectedStars = ArrayList<StarPoint>(1024)

        try {
            csvPath.inputStream().use {
                for (record in CSV_READER.ofNamedCsvRecord(InputStreamReader(it, Charsets.UTF_8))) {
                    val star = Star(
                        record.getField("x").toDouble(), record.getField("y").toDouble(),
                        record.getField("hfd").toDouble(), record.getField("snr").toDouble(),
                        record.getField("flux").toDouble(),
                    )

                    detectedStars.add(star)
                }
            }
        } finally {
            csvPath.deleteIfExists()
        }

        return detectedStars
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<AstapStarDetector>()

        @JvmStatic private val CSV_READER = CsvReader.builder()
            .fieldSeparator(',')
            .quoteCharacter('"')
            .commentCharacter('#')
            .commentStrategy(CommentStrategy.SKIP)
    }
}
