import io.kotest.core.annotation.EnabledIf
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import nebulosa.siril.livestacker.SirilLiveStacker
import nebulosa.test.AbstractFitsAndXisfTest
import nebulosa.test.NonGitHubOnlyCondition
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.listDirectoryEntries

@EnabledIf(NonGitHubOnlyCondition::class)
class SirilLiveStackerTest : AbstractFitsAndXisfTest() {

    init {
        "live stacking" {
            val executablePath = Path.of("siril-cli")
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
    }
}
