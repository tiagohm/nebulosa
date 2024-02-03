package nebulosa.astap.star.detection

import de.siegmar.fastcsv.reader.CommentStrategy
import de.siegmar.fastcsv.reader.CsvReader
import nebulosa.common.process.ProcessExecutor
import nebulosa.log.loggerFor
import nebulosa.star.detection.ImageStar
import nebulosa.star.detection.StarDetector
import java.io.InputStreamReader
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.nameWithoutExtension

class AstapStarDetector(path: Path) : StarDetector<Path> {

    private val executor = ProcessExecutor(path)

    override fun detect(input: Path): List<ImageStar> {
        val arguments = mutableMapOf<String, Any?>()

        arguments["-f"] = input
        arguments["-z"] = 2
        arguments["-extract"] = 0

        val process = executor.execute(arguments, workingDir = input.parent)

        LOG.info("astap exited. code={}", process.exitValue())

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
