import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.io.ByteOrder
import nebulosa.io.seekableSource
import nebulosa.test.NonGitHubOnlyCondition
import nebulosa.watney.plate.solving.quad.QuadDatabaseCellFileIndex
import java.nio.file.Path

@Suppress("NestedLambdaShadowedImplicitParameter")
@EnabledIf(NonGitHubOnlyCondition::class)
class QuadDatabaseTest : StringSpec() {

    init {
        "index" {
            val source = Path.of("/home/tiagohm/Downloads/watneyqdb-00-07-20-v3/gaia2-00-07-20.qdbindex").seekableSource()
            val index = QuadDatabaseCellFileIndex.read(source)
            index.byteOrder shouldBe ByteOrder.LITTLE
            val totalSizeInBytes = index.files.sumOf { it.descriptor.passes.sumOf { it.dataBlockByteLength } }
            totalSizeInBytes shouldBeExactly 397360102 - (406 * 13) // 406 files, 13 bytes header
            source.close()
        }
    }
}
