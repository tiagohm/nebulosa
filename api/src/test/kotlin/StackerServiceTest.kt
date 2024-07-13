import io.kotest.core.annotation.EnabledIf
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.api.stacker.*
import nebulosa.fits.fits
import nebulosa.image.Image
import nebulosa.image.algorithms.transformation.AutoScreenTransformFunction
import nebulosa.test.AbstractFitsAndXisfTest
import nebulosa.test.NonGitHubOnlyCondition
import java.nio.file.Path
import kotlin.io.path.createDirectories

@EnabledIf(NonGitHubOnlyCondition::class)
class StackerServiceTest : AbstractFitsAndXisfTest() {

    init {
        val service = StackerService()

        val paths = listOf(
            Path.of("$BASE_DIR/20240513.213424625-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213436506-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213448253-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213500627-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213512554-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213524278-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213535967-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213547683-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213559416-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213611421-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213624939-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213636654-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213648389-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213701880-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213713546-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213725316-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213738803-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213750501-LIGHT.fits"),
            Path.of("$BASE_DIR/20240513.213802188-LIGHT.fits"),
        )

        val darkPath = Path.of("/home/tiagohm/Imagens/Astrophotos/Dark/2024-06-08/ASI294_BIN4_G120_O80/10-DARK.fits")

        "stack LRGB" {
            val targets = paths.map {
                val analyzed = service.analyze(it)!!
                StackingTarget(true, it, analyzed.group, true)
            }

            targets.count { it.group == StackerGroupType.LUMINANCE } shouldBeExactly 10
            targets.count { it.group == StackerGroupType.RED } shouldBeExactly 3
            targets.count { it.group == StackerGroupType.GREEN } shouldBeExactly 3
            targets.count { it.group == StackerGroupType.BLUE } shouldBeExactly 3

            val request = StackingRequest(
                Path.of(BASE_DIR, "stacker").createDirectories(), StackerType.PIXINSIGHT,
                Path.of("PixInsight"), darkPath, true, null, false, null, false, false,
                1, paths[0], targets
            )

            val image = service.stack(request).shouldNotBeNull().fits().use(Image::open)
            image.transform(AutoScreenTransformFunction).save("stacker-lrgb").second shouldBe "465a296bb4582ab2f938757347500eb8"
        }
    }

    companion object {

        const val BASE_DIR = "/home/tiagohm/Imagens/Astrophotos/Light/Algieba/2024-05-13"
    }
}
