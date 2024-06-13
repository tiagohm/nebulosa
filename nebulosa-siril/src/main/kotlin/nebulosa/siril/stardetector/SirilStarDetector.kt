package nebulosa.siril.stardetector

import nebulosa.siril.command.FindStar
import nebulosa.siril.command.SirilCommandLine
import nebulosa.stardetector.StarDetector
import nebulosa.stardetector.StarPoint
import java.nio.file.Path

// https://gitlab.com/free-astro/siril/-/blob/master/src/algos/star_finder.c

data class SirilStarDetector(
    private val executablePath: Path,
    private val maxStars: Int = 0,
) : StarDetector<Path> {

    override fun detect(input: Path): List<StarPoint> {
        val commandLine = SirilCommandLine(executablePath)

        return commandLine.use {
            commandLine.run()
            commandLine.execute(FindStar(input, maxStars))
        }
    }
}
