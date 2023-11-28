import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import nebulosa.io.ByteOrder
import nebulosa.test.NonGitHubOnlyCondition
import nebulosa.watney.plate.solving.quad.CompactQuadDatabase
import nebulosa.watney.plate.solving.quad.QuadDatabaseCellFileIndex
import java.nio.file.Path

@Suppress("NestedLambdaShadowedImplicitParameter")
@EnabledIf(NonGitHubOnlyCondition::class)
class QuadDatabaseTest : StringSpec() {

    init {
        val quadDir = Path.of("/home/tiagohm/Downloads/watneyqdb-00-07-20-v3")

        "cell index file" {
            val source = Path.of("$quadDir", "gaia2-00-07-20.qdbindex")
            val index = QuadDatabaseCellFileIndex.read(source)

            index.byteOrder shouldBe ByteOrder.LITTLE
            index.files shouldHaveSize 406
            index.files.forEach { it.descriptor.path.toString().shouldContain(it.descriptor.id) shouldEndWith ".qdb" }
            val totalSizeInBytes = index.files.sumOf { it.descriptor.passes.sumOf { it.dataBlockByteLength } }
            totalSizeInBytes shouldBeExactly 397360102 - (406 * 13) // 406 files, 13 bytes header
        }
        "compact quad database" {
            val quadDatabase = CompactQuadDatabase(quadDir)
        }
    }
}
