import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.time.DeltaTime
import nebulosa.time.TDBMinusTTByFairheadAndBretagnon1990
import nebulosa.time.TT
import nebulosa.time.TimeJD

class TDBMinusTTByFairheadAndBretagnon1990Test : StringSpec(), DeltaTime by TDBMinusTTByFairheadAndBretagnon1990 {

    init {
        "delta" {
            delta(TT(TimeJD(1721119.0))) shouldBe (0.001745399870818484 plusOrMinus 1e-18)
            delta(TT(TimeJD(1757644.0))) shouldBe (0.0017585663326505057 plusOrMinus 1e-18)
            delta(TT(TimeJD(1794169.0))) shouldBe (0.0017239759121096452 plusOrMinus 1e-18)
            delta(TT(TimeJD(1830694.0))) shouldBe (0.0017498696535256737 plusOrMinus 1e-18)
            delta(TT(TimeJD(1867220.0))) shouldBe (0.0017159720952056887 plusOrMinus 1e-18)
            delta(TT(TimeJD(1903745.0))) shouldBe (0.0017311002643655385 plusOrMinus 1e-18)
            delta(TT(TimeJD(1940270.0))) shouldBe (0.0017117292854941177 plusOrMinus 1e-18)
            delta(TT(TimeJD(1976795.0))) shouldBe (0.0017078872927946418 plusOrMinus 1e-18)
            delta(TT(TimeJD(2013321.0))) shouldBe (0.001708107291615314 plusOrMinus 1e-18)
            delta(TT(TimeJD(2049846.0))) shouldBe (0.001688543416035648 plusOrMinus 1e-18)
            delta(TT(TimeJD(2086371.0))) shouldBe (0.0016985524502252747 plusOrMinus 1e-18)
            delta(TT(TimeJD(2122896.0))) shouldBe (0.0016671549420499388 plusOrMinus 1e-18)
            delta(TT(TimeJD(2159422.0))) shouldBe (0.001689058234231034 plusOrMinus 1e-18)
            delta(TT(TimeJD(2195947.0))) shouldBe (0.0016543375427534907 plusOrMinus 1e-18)
            delta(TT(TimeJD(2232472.0))) shouldBe (0.0016664671468315378 plusOrMinus 1e-18)
            delta(TT(TimeJD(2268997.0))) shouldBe (0.0016460669074697448 plusOrMinus 1e-18)
            delta(TT(TimeJD(2305523.0))) shouldBe (0.00163815496031778 plusOrMinus 1e-18)
            delta(TT(TimeJD(2342048.0))) shouldBe (0.0016486477103321029 plusOrMinus 1e-18)
            delta(TT(TimeJD(2378573.0))) shouldBe (0.0016048262600359682 plusOrMinus 1e-18)
            delta(TT(TimeJD(2415098.0))) shouldBe (0.0016353205418310752 plusOrMinus 1e-18)
            delta(TT(TimeJD(2451624.0))) shouldBe (0.0015927188479175568 plusOrMinus 1e-18)
            delta(TT(TimeJD(2488149.0))) shouldBe (0.0016091738905796783 plusOrMinus 1e-18)
            delta(TT(TimeJD(2524674.0))) shouldBe (0.0015892688800886133 plusOrMinus 1e-18)
            delta(TT(TimeJD(2561199.0))) shouldBe (0.001565799260750581 plusOrMinus 1e-18)
            delta(TT(TimeJD(2597725.0))) shouldBe (0.0015924659268900278 plusOrMinus 1e-18)
            delta(TT(TimeJD(2634250.0))) shouldBe (0.0015398074309449453 plusOrMinus 1e-18)
            delta(TT(TimeJD(2670775.0))) shouldBe (0.0015693003679969478 plusOrMinus 1e-18)
            delta(TT(TimeJD(2707300.0))) shouldBe (0.0015238654360012326 plusOrMinus 1e-18)
            delta(TT(TimeJD(2743826.0))) shouldBe (0.0015382035683946284 plusOrMinus 1e-18)
            delta(TT(TimeJD(2780351.0))) shouldBe (0.0015253666001684234 plusOrMinus 1e-18)
            delta(TT(TimeJD(2816876.0))) shouldBe (0.0014973592467537992 plusOrMinus 1e-18)
        }
    }
}
