package nebulosa.astap.star.detection

import de.siegmar.fastcsv.reader.CommentStrategy
import de.siegmar.fastcsv.reader.CsvReader
import nebulosa.common.exec.commandLine
import nebulosa.log.loggerFor
import nebulosa.star.detection.ImageStar
import nebulosa.star.detection.StarDetector
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

    override fun detect(input: Path): List<ImageStar> {
        val cmd = commandLine {
            executablePath(executablePath)
            workingDirectory(input.parent)

            putArg("-f", input)
            putArg("-z", "0")
            putArg("-extract", "$minSNR")
        }

        try {
            cmd.start()
            LOG.info("astap exited. code={}", cmd.get())
        } catch (e: Throwable) {
            LOG.error("astap failed", e)
            return emptyList()
        }

        val csvPath = Path.of("${input.parent}", "${input.nameWithoutExtension}.csv")

        if (!csvPath.exists()) return emptyList()

        val detectedStars = ArrayList<ImageStar>(1024)

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
