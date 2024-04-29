import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import nebulosa.api.livestacking.SirilLiveStacker
import nebulosa.test.NonGitHubOnlyCondition
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

@EnabledIf(NonGitHubOnlyCondition::class)
class SirilLiveStackerTest : StringSpec() {

    init {
        "live stacking" {
            val executable = Path.of("siril-cli")
            val workingDir = Path.of("/home/tiagohm/Git/nebulosa/data")
            val siril = SirilLiveStacker(executable, workingDir)
            siril.start()

            val fitsDir = Path.of("/home/tiagohm/Imagens/Astrophotos/Light/NGC2070/2024-04-20")

            for (fits in fitsDir.listDirectoryEntries()) {
                siril.add(fits)
                Thread.sleep(1000)
            }

            siril.stop()
        }
    }
}
