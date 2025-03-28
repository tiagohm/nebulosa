package nebulosa.astap.stardetector

import de.siegmar.fastcsv.reader.CommentStrategy
import de.siegmar.fastcsv.reader.CsvReader
import nebulosa.commandline.CommandLine
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.stardetector.StarDetector
import nebulosa.stardetector.StarPoint
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
        val commands = mutableListOf(
            "$executablePath",
            "-f", "$input",
            "-z", "0",
            "-extract", "$minSNR"
        )

        val commandline = CommandLine(commands, input.parent)
        val result = commandline.execute()

        if (result.isSuccess) {
            LOG.d { info("astap exited. code={}", result.exitCode) }
        } else {
            LOG.error("astap failed. code={}", result.exitCode)
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

        private val LOG = loggerFor<AstapStarDetector>()

        private val CSV_READER = CsvReader.builder()
            .fieldSeparator(',')
            .quoteCharacter('"')
            .commentCharacter('#')
            .commentStrategy(CommentStrategy.SKIP)
    }
}
