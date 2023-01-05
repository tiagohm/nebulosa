import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import nebulosa.desktop.mounts.MountManagerScreen

class GlobalTest : StringSpec() {

    init {
        "parse decimal coordinates" {
            MountManagerScreen.parseCoordinates("23.5634453") shouldBe 23.5634453
        }
        "parse sexagesimal coordinates" {
            MountManagerScreen.parseCoordinates("23 33 48.40308") shouldBe 23.5634453
            MountManagerScreen.parseCoordinates("23h 33 48.40308") shouldBe 23.5634453
            MountManagerScreen.parseCoordinates("23 33m 48.40308") shouldBe 23.5634453
            MountManagerScreen.parseCoordinates("23 33 48.40308s") shouldBe 23.5634453
            MountManagerScreen.parseCoordinates("23h 33m 48.40308") shouldBe 23.5634453
            MountManagerScreen.parseCoordinates("23 33m 48.40308s") shouldBe 23.5634453
            MountManagerScreen.parseCoordinates("23h 33m 48.40308s") shouldBe 23.5634453
            MountManagerScreen.parseCoordinates("-23° 33m 48.40308s") shouldBe -23.5634453
            MountManagerScreen.parseCoordinates("  -23   33m   48.40308s  ") shouldBe -23.5634453
            MountManagerScreen.parseCoordinates("-23 33.806718m") shouldBe -23.5634453
            MountManagerScreen.parseCoordinates("+23") shouldBe 23.0
            MountManagerScreen.parseCoordinates("-23") shouldBe -23.0
            MountManagerScreen.parseCoordinates("23h33m48.40308s") shouldBe 23.5634453
            MountManagerScreen.parseCoordinates("23h33m 48.40308\"") shouldBe 23.5634453
            MountManagerScreen.parseCoordinates("23h33'48.40308\"") shouldBe 23.5634453
            MountManagerScreen.parseCoordinates("-23°33'48.40308\"") shouldBe -23.5634453
            MountManagerScreen.parseCoordinates("-23°33'48.40308s 67.99") shouldBe -23.5634453
            MountManagerScreen.parseCoordinates("- 23°33'48.40308s 67.99") shouldBe -23.5634453
            MountManagerScreen.parseCoordinates("") shouldBe null
            MountManagerScreen.parseCoordinates("kkk") shouldBe null
        }
    }
}
