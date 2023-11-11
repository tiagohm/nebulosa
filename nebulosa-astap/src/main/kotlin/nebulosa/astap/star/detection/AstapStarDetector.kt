package nebulosa.astap.star.detection

import de.siegmar.fastcsv.reader.NamedCsvReader
import nebulosa.common.process.ProcessExecutor
import nebulosa.log.loggerFor
import nebulosa.star.detection.DetectedStar
import nebulosa.star.detection.StarDetector
import java.io.InputStreamReader
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.nameWithoutExtension

class AstapStarDetector(path: Path) : StarDetector {

    private val executor = ProcessExecutor(path)

    override fun detectStars(path: Path): Collection<DetectedStar> {
        val arguments = mutableMapOf<String, Any?>()

        arguments["-f"] = path
        arguments["-z"] = 2
        arguments["-extract"] = 0

        val process = executor.execute(arguments, workingDir = path.parent)

        LOG.info("astap exited. code={}", process.exitValue())

        val csvFile = Path.of("${path.parent}", path.nameWithoutExtension + ".csv")

        if (!csvFile.exists()) return emptyList()

        val detectedStars = ArrayList<DetectedStar>(512)

        try {
            csvFile.inputStream().use {
                for (record in CSV_READER.build(InputStreamReader(it, Charsets.UTF_8))) {
                    detectedStars.add(
                        DetectedStar(
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

        @JvmStatic private val CSV_READER = NamedCsvReader.builder()
            .fieldSeparator(',')
            .quoteCharacter('"')
            .commentCharacter('#')
            .skipComments(true)
    }
}
