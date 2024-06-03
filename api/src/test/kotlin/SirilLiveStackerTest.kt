import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import nebulosa.siril.livestacking.SirilLiveStacker
import nebulosa.test.NonGitHubOnlyCondition
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

@EnabledIf(NonGitHubOnlyCondition::class)
class SirilLiveStackerTest : StringSpec() {

    init {
        "live stacking" {
            val executablePath = Path.of("siril-cli")
            val workingDirectory = Path.of("/home/tiagohm/Git/nebulosa/data/siril")

            val siril = SirilLiveStacker(executablePath, workingDirectory)
            siril.start()

            val fitsDir = Path.of("/home/tiagohm/Imagens/Astrophotos/Light/C2023_A3/2024-05-29")

            for (fits in fitsDir.listDirectoryEntries().drop(140).sorted()) {
                siril.add(fits)
            }

            siril.stop()
        }
    }
}
