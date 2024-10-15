import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.math.*
import nebulosa.platesolver.Parity
import nebulosa.siril.livestacker.SirilLiveStacker
import nebulosa.siril.platesolver.SirilPlateSolver
import nebulosa.siril.stardetector.SirilStarDetector
import nebulosa.test.AbstractTest
import nebulosa.test.NonGitHubOnly
import nebulosa.test.concat
import nebulosa.test.dataDirectory
import nebulosa.test.fits.*
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.listDirectoryEntries

@NonGitHubOnly
class SirilTest : AbstractTest() {

    @Test
    fun liveStacking() {
        val workingDirectory = dataDirectory.concat("/siril")

        SirilLiveStacker(EXECUTABLE_PATH, workingDirectory).use {
            it.start()

            val inputDir = tempDirectory("ls-")

            STACKING_LIGHT_MONO_01_FITS.copyTo(inputDir.concat("01.fits"))
            STACKING_LIGHT_MONO_02_FITS.copyTo(inputDir.concat("02.fits"))
            STACKING_LIGHT_MONO_03_FITS.copyTo(inputDir.concat("03.fits"))
            STACKING_LIGHT_MONO_04_FITS.copyTo(inputDir.concat("04.fits"))

            for (fits in inputDir.listDirectoryEntries().shouldHaveSize(4).sorted()) {
                it.add(fits).shouldNotBeNull()
            }

            workingDirectory.listDirectoryEntries().shouldHaveSize(5)
        }

        workingDirectory.listDirectoryEntries().shouldBeEmpty()
    }

    @Test
    fun plateSolver() {
        val solution = SOLVER.solve(STACKING_LIGHT_MONO_01_FITS, null)
        solution.solved.shouldBeTrue()
        solution.orientation.toDegrees shouldBe (-90.02 plusOrMinus 1e-2)
        solution.rightAscension.formatHMS() shouldBe "00h06m46.0s"
        solution.declination.formatSignedDMS() shouldBe "+089°51'42.0\""
        solution.scale.toArcsec shouldBe (3.575 plusOrMinus 1e-3)
        solution.width.formatDMS() shouldBe "001°16'16.3\""
        solution.height.formatDMS() shouldBe "001°01'01.1\""
        solution.parity shouldBe Parity.FLIPPED
        solution.widthInPixels shouldBeExactly 1280.0
        solution.heightInPixels shouldBeExactly 1024.0
    }

    @Test
    fun starDetector() {
        val detector = SirilStarDetector(EXECUTABLE_PATH)
        val detectedStars = detector.detect(ASTROMETRY_GALACTIC_CENTER_FITS)
        detectedStars shouldHaveSize 425
        (detectedStars.sumOf { it.hfd } / detectedStars.size) shouldBe (2.1 plusOrMinus 0.1)
    }

    companion object {

        @JvmStatic private val EXECUTABLE_PATH = Path.of("siril-cli")
        @JvmStatic private val SOLVER = SirilPlateSolver(EXECUTABLE_PATH)
    }
}
