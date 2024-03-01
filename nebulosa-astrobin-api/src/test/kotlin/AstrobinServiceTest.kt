import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.astrobin.api.AstrobinService
import nebulosa.astrobin.api.SensorColor
import nebulosa.test.NonGitHubOnlyCondition

@EnabledIf(NonGitHubOnlyCondition::class)
class AstrobinServiceTest : StringSpec() {

    init {
        val service = AstrobinService()

        "sensors" {
            val page = service.sensors(1).execute().body()!!

            page.count shouldBeExactly 469
            page.results.size shouldBeExactly 50

            println(page.results[0])
        }
        "sensor" {
            val sensor = service.sensor(184).execute().body()!!

            sensor.id shouldBeExactly 184
            sensor.brandName shouldBe "Sony"
            sensor.name shouldBe "IMX492 (mono)"
            sensor.quantumEfficiency shouldBeExactly 90.0
            sensor.pixelSize shouldBeExactly 2.32
            sensor.pixelWidth shouldBeExactly 8240
            sensor.pixelHeight shouldBeExactly 5628
            sensor.readNoise shouldBeExactly 1.3
            sensor.fullWellCapacity shouldBeExactly 14.0
            sensor.adc shouldBeExactly 12
            sensor.color shouldBe SensorColor.MONO
            sensor.cameras.toList().shouldContain(529)
        }
        "cameras" {
            val page = service.cameras(1).execute().body()!!

            page.count shouldBeExactly 3362
            page.results.size shouldBeExactly 50

            println(page.results[0])
        }
        "camera" {
            val camera = service.camera(529).execute().body()!!

            camera.id shouldBeExactly 529
            camera.brandName shouldBe "ZWO"
            camera.name shouldBe "ASI294MM"
            camera.cooled.shouldBeFalse()
            camera.sensor shouldBeExactly 184
        }
        "telescopes" {
            val page = service.telescopes(1).execute().body()!!

            page.count shouldBeExactly 3813
            page.results.size shouldBeExactly 50

            println(page.results[0])
        }
        "telescope" {
            val telescope = service.telescope(1097).execute().body()!!

            telescope.id shouldBeExactly 1097
            telescope.brandName shouldBe "GSO"
            telescope.name shouldBe "6\" f/9 Ritchey-Chretien"
            telescope.aperture shouldBeExactly 152.0
            telescope.minFocalLength shouldBeExactly 1368.0
            telescope.maxFocalLength shouldBeExactly 1368.0
        }
    }
}
