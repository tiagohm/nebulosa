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

data class AstapStarDetector(private val executablePath: Path) : StarDetector<Path> {

    override fun detect(input: Path): List<ImageStar> {
        val cmd = commandLine {
            executablePath(executablePath)
            workingDirectory(input.parent)

            putArg("-f", input)
            putArg("-z", "2")
            putArg("-extract", "0")
        }

        try {
            cmd.start()

            LOG.info("astap exited. code={}", cmd.get())
        } catch (e: Throwable) {
            return emptyList()
        }

        val csvFile = Path.of("${input.parent}", input.nameWithoutExtension + ".csv")

        if (!csvFile.exists()) return emptyList()

        val detectedStars = ArrayList<ImageStar>(512)

        try {
            csvFile.inputStream().use {
                for (record in CSV_READER.ofNamedCsvRecord(InputStreamReader(it, Charsets.UTF_8))) {
                    detectedStars.add(
                        Star(
                            record.getField("x").toDouble(),
                            record.getField("y").toDouble(),
                            record.getField("hfd").toDouble(),
                            record.getField("snr").toDouble(),
                            record.getField("flux").toDouble(),
                        )
                    )
                }
            }
        } finally {
            csvFile.deleteIfExists()
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
