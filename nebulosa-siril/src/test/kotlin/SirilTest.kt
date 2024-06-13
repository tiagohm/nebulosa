import io.kotest.core.annotation.EnabledIf
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.booleans.shouldBeTrue
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
import nebulosa.test.AbstractFitsAndXisfTest
import nebulosa.test.NonGitHubOnlyCondition
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.listDirectoryEntries

@EnabledIf(NonGitHubOnlyCondition::class)
class SirilTest : AbstractFitsAndXisfTest() {

    init {
        val executablePath = Path.of("siril-cli")

        "live stacking" {
            val workingDirectory = Path.of("/home/tiagohm/Git/nebulosa/data/siril")

            SirilLiveStacker(executablePath, workingDirectory).use {
                it.start()

                val fitsDir = tempdir().toPath()

                PI_01_LIGHT.copyTo(Path.of("$fitsDir", "01.fits"))
                PI_02_LIGHT.copyTo(Path.of("$fitsDir", "02.fits"))
                PI_03_LIGHT.copyTo(Path.of("$fitsDir", "03.fits"))
                PI_04_LIGHT.copyTo(Path.of("$fitsDir", "04.fits"))

                for (fits in fitsDir.listDirectoryEntries().shouldHaveSize(4).sorted()) {
                    it.add(fits).shouldNotBeNull()
                }

                workingDirectory.listDirectoryEntries().shouldHaveSize(5)
            }

            workingDirectory.listDirectoryEntries().shouldHaveSize(1)
        }
        "plate solver" {
            val solver = SirilPlateSolver(executablePath)
            val solution = solver.solve(PI_01_LIGHT, null)

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
        "star detector" {
            val detector = SirilStarDetector(executablePath)

            with(detector.detect(PI_FOCUS_0)) {
                this shouldHaveSize 307
                map { it.hfd }.average() shouldBe (7.9 plusOrMinus 1e-1)
            }

            with(detector.detect(PI_FOCUS_30000)) {
                this shouldHaveSize 258
                map { it.hfd }.average() shouldBe (1.1 plusOrMinus 1e-1)
            }

            with(detector.detect(PI_FOCUS_100000)) {
                this shouldHaveSize 82
                map { it.hfd }.average() shouldBe (22.4 plusOrMinus 1e-1)
            }
        }
    }
}
