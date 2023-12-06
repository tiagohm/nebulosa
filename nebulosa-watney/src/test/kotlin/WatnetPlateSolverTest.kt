import io.kotest.core.annotation.EnabledIf
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.imaging.Image
import nebulosa.math.deg
import nebulosa.test.Hips2FitsStringSpec
import nebulosa.test.NonGitHubOnlyCondition
import nebulosa.watney.plate.solving.WatneyPlateSolver
import nebulosa.watney.plate.solving.quad.CompactQuadDatabase
import nebulosa.watney.star.detection.Star
import java.nio.file.Path

@EnabledIf(NonGitHubOnlyCondition::class)
class WatnetPlateSolverTest : Hips2FitsStringSpec() {

    init {
        val quadDir = Path.of("/home/tiagohm/Downloads/watneyqdb")
        val quadDatabase = CompactQuadDatabase(quadDir)
        val solver = WatneyPlateSolver(quadDatabase)

        "blind" {
            val image = Image.open(M31)
            println(solver.solve(image, 0.0, 0.0, 0.deg))
        }
        "form image star quads" {
            val (quads, countInFirstPass) = WatneyPlateSolver.formImageStarQuads(M31_STARS)
            quads shouldHaveSize 42
            countInFirstPass shouldBeExactly 36

            // Original Quads generated by Watney C# implementation using the M31 image.

            val origQuadStars = listOf(
                doubleArrayOf(358.5, 0.5, 399.5, 58.5, 371.5, 164.0, 487.5, 108.0),
                doubleArrayOf(530.0, 3.0, 514.5, 110.0, 487.5, 108.0, 399.5, 58.5),
                doubleArrayOf(957.0, 3.5, 971.5, 6.5, 1034.0, 192.0, 985.5, 240.5),
                doubleArrayOf(514.5, 110.0, 487.5, 108.0, 566.5, 203.5, 530.0, 3.0),
                doubleArrayOf(697.0, 111.5, 642.5, 187.0, 657.0, 224.5, 566.5, 203.5),
                doubleArrayOf(371.5, 164.0, 399.5, 58.5, 487.5, 108.0, 514.5, 110.0),
                doubleArrayOf(17.0, 169.5, 82.5, 180.5, 70.5, 283.5, 114.0, 284.0),
                doubleArrayOf(642.5, 187.0, 657.0, 224.5, 599.5, 248.5, 566.5, 203.5),
                doubleArrayOf(1034.0, 192.0, 985.5, 240.5, 1060.0, 287.0, 971.5, 6.5),
                doubleArrayOf(566.5, 203.5, 599.5, 248.5, 583.5, 259.5, 642.5, 187.0),
                doubleArrayOf(657.0, 224.5, 642.5, 187.0, 599.5, 248.5, 583.5, 259.5),
                doubleArrayOf(985.5, 240.5, 1034.0, 192.0, 1060.0, 287.0, 1053.0, 426.0),
                doubleArrayOf(599.5, 248.5, 583.5, 259.5, 566.5, 203.5, 657.0, 224.5),
                doubleArrayOf(583.5, 259.5, 599.5, 248.5, 574.5, 309.5, 566.5, 203.5),
                doubleArrayOf(70.5, 283.5, 114.0, 284.0, 126.5, 300.5, 133.0, 305.0),
                doubleArrayOf(5.5, 321.0, 70.5, 283.5, 114.0, 284.0, 126.5, 300.5),
                doubleArrayOf(403.5, 326.5, 474.5, 379.0, 271.0, 374.0, 372.5, 474.5),
                doubleArrayOf(240.5, 370.0, 271.0, 374.0, 248.5, 462.0, 133.0, 305.0),
                doubleArrayOf(271.0, 374.0, 240.5, 370.0, 248.5, 462.0, 403.5, 326.5),
                doubleArrayOf(474.5, 379.0, 403.5, 326.5, 540.5, 460.0, 574.5, 309.5),
                doubleArrayOf(738.5, 402.5, 686.5, 411.5, 616.5, 459.0, 828.0, 539.0),
                doubleArrayOf(1258.5, 410.0, 1196.5, 573.0, 1208.0, 587.5, 1053.0, 426.0),
                doubleArrayOf(686.5, 411.5, 738.5, 402.5, 616.5, 459.0, 574.5, 309.5),
                doubleArrayOf(1053.0, 426.0, 1060.0, 287.0, 985.5, 240.5, 1196.5, 573.0),
                doubleArrayOf(616.5, 459.0, 540.5, 460.0, 686.5, 411.5, 533.5, 488.0),
                doubleArrayOf(540.5, 460.0, 533.5, 488.0, 616.5, 459.0, 474.5, 379.0),
                doubleArrayOf(248.5, 462.0, 195.0, 503.0, 271.0, 374.0, 240.5, 370.0),
                doubleArrayOf(372.5, 474.5, 365.5, 492.5, 248.5, 462.0, 474.5, 379.0),
                doubleArrayOf(365.5, 492.5, 372.5, 474.5, 248.5, 462.0, 271.0, 374.0),
                doubleArrayOf(828.0, 539.0, 738.5, 402.5, 680.5, 656.0, 686.5, 411.5),
                doubleArrayOf(24.0, 563.0, 195.0, 503.0, 188.5, 655.5, 5.5, 321.0),
                doubleArrayOf(1196.5, 573.0, 1208.0, 587.5, 1066.5, 642.5, 1258.5, 410.0),
                doubleArrayOf(652.5, 637.5, 651.0, 655.0, 680.5, 656.0, 524.0, 691.0),
                doubleArrayOf(1066.5, 642.5, 1196.5, 573.0, 1208.0, 587.5, 1053.0, 426.0),
                doubleArrayOf(188.5, 655.5, 296.5, 658.0, 195.0, 503.0, 24.0, 563.0),
                doubleArrayOf(296.5, 658.0, 188.5, 655.5, 365.5, 492.5, 195.0, 503.0),
                doubleArrayOf(828.0, 539.0, 738.5, 402.5, 686.5, 411.5, 652.5, 637.5),
                doubleArrayOf(652.5, 637.5, 651.0, 655.0, 616.5, 459.0, 533.5, 488.0),
                doubleArrayOf(188.5, 655.5, 195.0, 503.0, 24.0, 563.0, 248.5, 462.0),
                doubleArrayOf(24.0, 563.0, 195.0, 503.0, 5.5, 321.0, 248.5, 462.0),
                doubleArrayOf(652.5, 637.5, 616.5, 459.0, 533.5, 488.0, 828.0, 539.0),
                doubleArrayOf(1258.5, 410.0, 1196.5, 573.0, 1053.0, 426.0, 1060.0, 287.0),
            )

            val origQuads = listOf(
                doubleArrayOf(0.4229872, 0.6012765, 0.6500249, 0.7670894, 0.9767487, 167.92, 404.25, 82.75),
                doubleArrayOf(0.19091523, 0.7119774, 0.76239824, 0.7987724, 0.8885384, 141.81, 482.88, 69.88),
                doubleArrayOf(0.06203029, 0.28733647, 0.82002467, 0.85301214, 0.9820323, 238.71, 987.0, 110.63),
                doubleArrayOf(0.1328489, 0.52497375, 0.530517, 0.55582803, 0.6081619, 203.8, 524.63, 106.13),
                doubleArrayOf(0.25180638, 0.48707265, 0.5818563, 0.58317775, 0.75074446, 159.67, 640.75, 181.63),
                doubleArrayOf(0.17712061, 0.66053337, 0.7140859, 0.8243369, 0.84268737, 152.86, 443.25, 110.13),
                doubleArrayOf(0.28989518, 0.44259232, 0.6910156, 0.7209406, 0.8391714, 150.06, 71.0, 229.38),
                doubleArrayOf(0.43276387, 0.6006513, 0.67066383, 0.80772877, 0.8371013, 92.9, 616.38, 215.88),
                doubleArrayOf(0.23319396, 0.29857823, 0.3348643, 0.66550833, 0.79698896, 294.13, 1012.75, 181.5),
                doubleArrayOf(0.20772238, 0.59699667, 0.6260989, 0.8028142, 0.83200794, 93.47, 598.0, 224.63),
                doubleArrayOf(0.20772238, 0.43013072, 0.6665832, 0.8028142, 0.8709221, 93.47, 620.63, 229.88),
                doubleArrayOf(0.2921554, 0.37407166, 0.41953236, 0.59281886, 0.8408198, 234.77, 1033.13, 286.38),
                doubleArrayOf(0.208994, 0.6006513, 0.6299317, 0.67066383, 0.8762536, 92.9, 601.63, 234.0),
                doubleArrayOf(0.18265495, 0.47791952, 0.5249526, 0.5505428, 0.6201627, 106.3, 581.0, 255.25),
                doubleArrayOf(0.11961175, 0.31319097, 0.42847058, 0.6581907, 0.8854501, 66.09, 111.0, 293.25),
                doubleArrayOf(0.16867274, 0.35447648, 0.47686976, 0.6114654, 0.93408805, 122.72, 79.13, 297.25),
                doubleArrayOf(0.43378574, 0.6864228, 0.69147134, 0.7016915, 0.74283123, 203.56, 380.38, 388.5),
                doubleArrayOf(0.15782383, 0.46601853, 0.473798, 0.6445258, 0.79159623, 194.91, 223.25, 377.75),
                doubleArrayOf(0.14941548, 0.44119054, 0.44855553, 0.6836948, 0.8194446, 205.88, 290.88, 383.13),
                doubleArrayOf(0.46161732, 0.5462143, 0.6366278, 0.8065975, 0.8983449, 191.29, 498.25, 368.75),
                doubleArrayOf(0.23338081, 0.37410653, 0.5945751, 0.7218385, 0.8423202, 226.12, 717.38, 453.0),
                doubleArrayOf(0.082676105, 0.77907515, 0.8244223, 0.9177246, 0.92081827, 223.85, 1179.0, 499.13),
                doubleArrayOf(0.2799132, 0.44869733, 0.71312374, 0.803495, 0.8236593, 188.53, 654.0, 395.63),
                doubleArrayOf(0.22300959, 0.35341972, 0.50126994, 0.5216613, 0.80473685, 393.8, 1073.75, 381.63),
                doubleArrayOf(0.16872369, 0.44432908, 0.4945342, 0.5139766, 0.8993664, 171.06, 594.25, 454.63),
                doubleArrayOf(0.17708255, 0.4663419, 0.53943986, 0.64106923, 0.76046133, 162.98, 541.25, 446.5),
                doubleArrayOf(0.20545381, 0.45018867, 0.60665923, 0.6167865, 0.9388506, 149.72, 238.75, 427.25),
                doubleArrayOf(0.080217935, 0.50220335, 0.5176477, 0.58036906, 0.6536133, 240.76, 365.25, 452.0),
                doubleArrayOf(0.12742372, 0.5992795, 0.79773456, 0.82226735, 0.942405, 151.57, 314.38, 450.75),
                doubleArrayOf(0.20293406, 0.6276678, 0.7239713, 0.7324315, 0.9404851, 260.05, 733.38, 502.25),
                doubleArrayOf(0.40032506, 0.47528812, 0.4949649, 0.63654554, 0.6890982, 381.29, 103.25, 510.63),
                doubleArrayOf(0.0613762, 0.48887974, 0.50347656, 0.57836145, 0.61202574, 301.53, 1182.38, 553.25),
                doubleArrayOf(0.10952552, 0.18405987, 0.20926912, 0.8231408, 0.86796653, 160.37, 627.0, 659.88),
                doubleArrayOf(0.082676105, 0.6585399, 0.67820233, 0.9177246, 0.96905917, 223.85, 1131.0, 557.25),
                doubleArrayOf(0.3743402, 0.5289205, 0.6279637, 0.64201605, 0.65396124, 288.58, 176.0, 594.88),
                doubleArrayOf(0.44896066, 0.63435477, 0.70992845, 0.7451902, 0.7699947, 240.62, 261.38, 577.25),
                doubleArrayOf(0.21088836, 0.65227014, 0.7611402, 0.8042312, 0.9132892, 250.24, 726.38, 497.63),
                doubleArrayOf(0.086017, 0.43057266, 0.8917693, 0.935772, 0.9746273, 204.19, 613.38, 559.88),
                doubleArrayOf(0.27380574, 0.62004495, 0.7361518, 0.76662827, 0.82295257, 246.17, 164.0, 545.88),
                doubleArrayOf(0.23991768, 0.64504075, 0.8638924, 0.8762333, 0.9352145, 280.94, 118.25, 462.25),
                doubleArrayOf(0.294163, 0.609248, 0.63931024, 0.6733474, 0.75656414, 298.88, 657.63, 530.88),
                doubleArrayOf(0.43917423, 0.55030274, 0.64823836, 0.6504235, 0.7368766, 316.9, 1142.0, 424.0),
            )

            origQuads.forEachIndexed { i, q ->
                with(quads[i]) {
                    ratios[0] shouldBe (q[0] plusOrMinus 1e-7)
                    ratios[1] shouldBe (q[1] plusOrMinus 1e-7)
                    ratios[2] shouldBe (q[2] plusOrMinus 1e-7)
                    ratios[3] shouldBe (q[3] plusOrMinus 1e-7)
                    ratios[4] shouldBe (q[4] plusOrMinus 1e-7)
                    largestDistance shouldBe (q[5] plusOrMinus 1e-2)
                    midPointX shouldBe (q[6] plusOrMinus 1e-1)
                    midPointY shouldBe (q[7] plusOrMinus 1e-1)

                    stars[0].x shouldBe (origQuadStars[i][0] plusOrMinus 1e-1)
                    stars[0].y shouldBe (origQuadStars[i][1] plusOrMinus 1e-1)
                    stars[1].x shouldBe (origQuadStars[i][2] plusOrMinus 1e-1)
                    stars[1].y shouldBe (origQuadStars[i][3] plusOrMinus 1e-1)
                    stars[2].x shouldBe (origQuadStars[i][4] plusOrMinus 1e-1)
                    stars[2].y shouldBe (origQuadStars[i][5] plusOrMinus 1e-1)
                    stars[3].x shouldBe (origQuadStars[i][6] plusOrMinus 1e-1)
                    stars[3].y shouldBe (origQuadStars[i][7] plusOrMinus 1e-1)
                }
            }
        }
    }

    companion object {

        @JvmStatic private val M31_STARS = listOf(
            Star(358.5, 0.5), Star(530.0, 3.0), Star(957.0, 3.5),
            Star(971.5, 6.5), Star(399.5, 58.5), Star(487.5, 108.0),
            Star(514.5, 110.0), Star(697.0, 111.5), Star(371.5, 164.0),
            Star(17.0, 169.5), Star(642.5, 187.0), Star(82.5, 180.5),
            Star(1034.0, 192.0), Star(566.5, 203.5), Star(657.0, 224.5),
            Star(985.5, 240.5), Star(599.5, 248.5), Star(583.5, 259.5),
            Star(70.5, 283.5), Star(114.0, 284.0), Star(1060.0, 287.0),
            Star(126.5, 300.5), Star(133.0, 305.0), Star(574.5, 309.5),
            Star(5.5, 321.0), Star(403.5, 326.5), Star(240.5, 370.0),
            Star(271.0, 374.0), Star(474.5, 379.0), Star(738.5, 402.5),
            Star(1258.5, 410.0), Star(686.5, 411.5), Star(1053.0, 426.0),
            Star(616.5, 459.0), Star(540.5, 460.0), Star(248.5, 462.0),
            Star(372.5, 474.5), Star(533.5, 488.0), Star(365.5, 492.5),
            Star(195.0, 503.0), Star(828.0, 539.0), Star(24.0, 563.0),
            Star(1196.5, 573.0), Star(1208.0, 587.5), Star(652.5, 637.5),
            Star(1066.5, 642.5), Star(651.0, 655.0), Star(188.5, 655.5),
            Star(680.5, 656.0), Star(296.5, 658.0), Star(524.0, 691.0),
        )
    }
}
