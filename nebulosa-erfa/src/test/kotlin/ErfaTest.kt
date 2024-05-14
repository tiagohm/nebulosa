import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.constants.TAU
import nebulosa.erfa.*
import nebulosa.math.*

@Suppress("FloatingPointLiteralPrecision")
class ErfaTest : StringSpec() {

    init {
        "eraRx" {
            val r = Matrix3D(2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 4.0, 5.0)
            val s = r.rotateX(0.3456789.rad)

            s[0] shouldBe (2.0 plusOrMinus 1e-12)
            s[1] shouldBe (3.0 plusOrMinus 1e-12)
            s[2] shouldBe (2.0 plusOrMinus 1e-12)
            s[3] shouldBe (3.839043388235612460 plusOrMinus 1e-12)
            s[4] shouldBe (3.237033249594111899 plusOrMinus 1e-12)
            s[5] shouldBe (4.516714379005982719 plusOrMinus 1e-12)
            s[6] shouldBe (1.806030415924501684 plusOrMinus 1e-12)
            s[7] shouldBe (3.085711545336372503 plusOrMinus 1e-12)
            s[8] shouldBe (3.687721683977873065 plusOrMinus 1e-12)

            (s == Matrix3D.rotX(0.3456789.rad) * r).shouldBeTrue()
        }
        "eraRy" {
            val r = Matrix3D(2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 4.0, 5.0)
            val s = r.rotateY(0.3456789.rad)

            s[0] shouldBe (0.8651847818978159930 plusOrMinus 1e-12)
            s[1] shouldBe (1.467194920539316554 plusOrMinus 1e-12)
            s[2] shouldBe (0.1875137911274457342 plusOrMinus 1e-12)
            s[3] shouldBe (3.0 plusOrMinus 1e-12)
            s[4] shouldBe (2.0 plusOrMinus 1e-12)
            s[5] shouldBe (3.0 plusOrMinus 1e-12)
            s[6] shouldBe (3.500207892850427330 plusOrMinus 1e-12)
            s[7] shouldBe (4.779889022262298150 plusOrMinus 1e-12)
            s[8] shouldBe (5.381899160903798712 plusOrMinus 1e-12)

            (s == Matrix3D.rotY(0.3456789.rad) * r).shouldBeTrue()
        }
        "eraRz" {
            val r = Matrix3D(2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 4.0, 5.0)
            val s = r.rotateZ(0.3456789.rad)

            s[0] shouldBe (2.898197754208926769 plusOrMinus 1e-12)
            s[1] shouldBe (3.500207892850427330 plusOrMinus 1e-12)
            s[2] shouldBe (2.898197754208926769 plusOrMinus 1e-12)
            s[3] shouldBe (2.144865911309686813 plusOrMinus 1e-12)
            s[4] shouldBe (0.865184781897815993 plusOrMinus 1e-12)
            s[5] shouldBe (2.144865911309686813 plusOrMinus 1e-12)
            s[6] shouldBe (3.0 plusOrMinus 1e-12)
            s[7] shouldBe (4.0 plusOrMinus 1e-12)
            s[8] shouldBe (5.0 plusOrMinus 1e-12)

            (s == Matrix3D.rotZ(0.3456789.rad) * r).shouldBeTrue()
        }
        "eraRxr" {
            val a = Matrix3D(2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 4.0, 5.0)
            val b = Matrix3D(1.0, 2.0, 2.0, 4.0, 1.0, 1.0, 3.0, 0.0, 1.0)
            val c = a * b

            c[0] shouldBe (20.0 plusOrMinus 1e-12)
            c[1] shouldBe (7.0 plusOrMinus 1e-12)
            c[2] shouldBe (9.0 plusOrMinus 1e-12)
            c[3] shouldBe (20.0 plusOrMinus 1e-12)
            c[4] shouldBe (8.0 plusOrMinus 1e-12)
            c[5] shouldBe (11.0 plusOrMinus 1e-12)
            c[6] shouldBe (34.0 plusOrMinus 1e-12)
            c[7] shouldBe (10.0 plusOrMinus 1e-12)
            c[8] shouldBe (15.0 plusOrMinus 1e-12)
        }
        "eraC2s" {
            val (theta, phi) = eraC2s(100.0.au, (-50.0).au, 25.0.au)
            theta shouldBe (-0.4636476090008061162 plusOrMinus 1e-14)
            phi shouldBe (0.2199879773954594463 plusOrMinus 1e-14)
        }
        "eraS2c" {
            val c = eraS2c(3.0123, -0.999)
            c[0] shouldBe (-0.5366267667260523906 plusOrMinus 1e-12)
            c[1] shouldBe (0.0697711109765145365 plusOrMinus 1e-12)
            c[2] shouldBe (-0.8409302618566214041 plusOrMinus 1e-12)
        }
        "eraP2s" {
            val (theta, phi, r) = eraP2s(100.0.au, (-50.0).au, 25.0.au)
            theta shouldBe (-0.4636476090008061162 plusOrMinus 1e-12)
            phi shouldBe (0.2199879773954594463 plusOrMinus 1e-12)
            r shouldBe (114.5643923738960002 plusOrMinus 1e-9)
        }
        "eraAnpm" {
            eraAnpm((-4.0).rad) shouldBe (2.283185307179586477 plusOrMinus 1e-12)
        }
        "eraGc2Gde" {
            val (e1, p1, h1) = eraGc2Gde(6378137.0, 1.0 / 298.257223563, 2e6, 3e6, 5.244e6)
            e1 shouldBe (0.9827937232473290680 plusOrMinus 1e-14)
            p1 shouldBe (0.97160184819075459 plusOrMinus 1e-14)
            h1 shouldBe (331.4172461426059892 plusOrMinus 1e-8)

            val (e2, p2, h2) = eraGc2Gde(6378137.0, 1.0 / 298.257222101, 2e6, 3e6, 5.244e6)
            e2 shouldBe (0.9827937232473290680 plusOrMinus 1e-14)
            p2 shouldBe (0.97160184820607853 plusOrMinus 1e-14)
            h2 shouldBe (331.41731754844348 plusOrMinus 1e-8)

            val (e3, p3, h3) = eraGc2Gde(6378135.0, 1.0 / 298.26, 2e6, 3e6, 5.244e6)
            e3 shouldBe (0.9827937232473290680 plusOrMinus 1e-14)
            p3 shouldBe (0.9716018181101511937 plusOrMinus 1e-14)
            h3 shouldBe (333.2770726130318123 plusOrMinus 1e-8)
        }
        "eraGd2Gc" {
            val (x1, y1, z1) = eraGd2Gce(6378137.0, 1.0 / 298.257223563, 3.1.rad, (-0.5).rad, 2500.0)
            x1 shouldBe (-5599000.5577049947 plusOrMinus 1e-7)
            y1 shouldBe (233011.67223479203 plusOrMinus 1e-7)
            z1 shouldBe (-3040909.4706983363 plusOrMinus 1e-7)

            val (x2, y2, z2) = eraGd2Gce(6378137.0, 1.0 / 298.257222101, 3.1.rad, (-0.5).rad, 2500.0)
            x2 shouldBe (-5599000.5577260984 plusOrMinus 1e-7)
            y2 shouldBe (233011.6722356702949 plusOrMinus 1e-7)
            z2 shouldBe (-3040909.4706095476 plusOrMinus 1e-7)

            val (x3, y3, z3) = eraGd2Gce(6378135.0, 1.0 / 298.26, 3.1.rad, (-0.5).rad, 2500.0)
            x3 shouldBe (-5598998.7626301490 plusOrMinus 1e-7)
            y3 shouldBe (233011.5975297822211 plusOrMinus 1e-7)
            z3 shouldBe (-3040908.6861467111 plusOrMinus 1e-7)
        }
        "eraC2ixys" {
            val m = eraC2ixys(0.5791308486706011000e-3, 0.4020579816732961219e-4, (-0.1220040848472271978e-7).rad)
            m[0, 0] shouldBe (0.9999998323037157138 plusOrMinus 1e-12)
            m[0, 1] shouldBe (0.5581984869168499149e-9 plusOrMinus 1e-12)
            m[0, 2] shouldBe (-0.5791308491611282180e-3 plusOrMinus 1e-12)
            m[1, 0] shouldBe (-0.2384261642670440317e-7 plusOrMinus 1e-12)
            m[1, 1] shouldBe (0.9999999991917468964 plusOrMinus 1e-12)
            m[1, 2] shouldBe (-0.4020579110169668931e-4 plusOrMinus 1e-12)
            m[2, 0] shouldBe (0.5791308486706011000e-3 plusOrMinus 1e-12)
            m[2, 1] shouldBe (0.4020579816732961219e-4 plusOrMinus 1e-12)
            m[2, 2] shouldBe (0.9999998314954627590 plusOrMinus 1e-12)
        }
        "eraPom00" {
            val m = eraPom00(2.55060238e-7.rad, 1.860359247e-6.rad, (-0.1367174580728891460e-10).rad)
            m[0, 0] shouldBe (0.9999999999999674721 plusOrMinus 1e-12)
            m[0, 1] shouldBe (-0.1367174580728846989e-10 plusOrMinus 1e-12)
            m[0, 2] shouldBe (0.2550602379999972345e-6 plusOrMinus 1e-12)
            m[1, 0] shouldBe (0.1414624947957029801e-10 plusOrMinus 1e-12)
            m[1, 1] shouldBe (0.9999999999982695317 plusOrMinus 1e-12)
            m[1, 2] shouldBe (-0.1860359246998866389e-5 plusOrMinus 1e-12)
            m[2, 0] shouldBe (-0.2550602379741215021e-6 plusOrMinus 1e-12)
            m[2, 1] shouldBe (0.1860359247002414021e-5 plusOrMinus 1e-12)
            m[2, 2] shouldBe (0.9999999999982370039 plusOrMinus 1e-12)
        }
        "eraApcs" {
            val astro = eraApcs(
                2456384.5, 0.970031644,
                Vector3D(-1836024.09, 1056607.7, -5998795.26),
                Vector3D(-77.0361767, -133.310856, 0.0971855934),
                Vector3D(-0.974170438, -0.211520082, -0.0917583024),
                Vector3D(0.00364365824, -0.0154287319, -0.00668922024),
                Vector3D(-0.973458265, -0.209215307, -0.0906996477),
            )

            astro.pmt shouldBe (13.25248468622587269 plusOrMinus 1e-11)
            astro.eb.x shouldBe (-0.9741827110629881886 plusOrMinus 1e-12)
            astro.eb.y shouldBe (-0.2115130190136415986 plusOrMinus 1e-12)
            astro.eb.z shouldBe (-0.09179840186954412099 plusOrMinus 1e-12)
            astro.eh.x shouldBe (-0.9736425571689454706 plusOrMinus 1e-12)
            astro.eh.y shouldBe (-0.2092452125850435930 plusOrMinus 1e-12)
            astro.eh.z shouldBe (-0.09075578152248299218 plusOrMinus 1e-12)
            astro.em shouldBe (0.9998233241709796859 plusOrMinus 1e-12)
            astro.v.x shouldBe (0.2078704993282685510e-4 plusOrMinus 1e-16)
            astro.v.y shouldBe (-0.8955360106989405683e-4 plusOrMinus 1e-16)
            astro.v.z shouldBe (-0.3863338994289409097e-4 plusOrMinus 1e-16)
            astro.bm1 shouldBe (0.9999999950277561237 plusOrMinus 1e-12)
        }
        "eraApco" {
            val astro = eraApco(
                2456384.5, 0.970031644,
                Vector3D(-0.974170438, -0.211520082, -0.0917583024),
                Vector3D(0.00364365824, -0.0154287319, -0.00668922024),
                Vector3D(-0.973458265, -0.209215307, -0.0906996477),
                0.0013122272, -2.92808623e-5, 3.05749468e-8.rad,
                3.14540971.rad, (-0.527800806).rad, (-1.2345856).rad,
                2738.0,
                2.47230737e-7.rad, 1.82640464e-6.rad, (-3.01974337e-11).rad,
                0.000201418779.rad, (-2.36140831e-7).rad,
            )

            astro.pmt shouldBe (13.25248468622587269 plusOrMinus 1e-11)
            astro.eb.x shouldBe (-0.9741827110630322720 plusOrMinus 1e-12)
            astro.eb.y shouldBe (-0.2115130190135344832 plusOrMinus 1e-12)
            astro.eb.z shouldBe (-0.09179840186949532298 plusOrMinus 1e-12)
            astro.eh.x shouldBe (-0.9736425571689739035 plusOrMinus 1e-12)
            astro.eh.y shouldBe (-0.2092452125849330936 plusOrMinus 1e-12)
            astro.eh.z shouldBe (-0.09075578152243272599 plusOrMinus 1e-12)
            astro.em shouldBe (0.9998233241709957653 plusOrMinus 1e-12)
            astro.v.x shouldBe (0.2078704992916728762e-4 plusOrMinus 1e-16)
            astro.v.y shouldBe (-0.8955360107151952319e-4 plusOrMinus 1e-16)
            astro.v.z shouldBe (-0.3863338994288951082e-4 plusOrMinus 1e-16)
            astro.bm1 shouldBe (0.9999999950277561236 plusOrMinus 1e-12)
            astro.bpn[0, 0] shouldBe (0.9999991390295159156 plusOrMinus 1e-12)
            astro.bpn[1, 0] shouldBe (0.4978650072505016932e-7 plusOrMinus 1e-12)
            astro.bpn[2, 0] shouldBe (0.1312227200000000000e-2 plusOrMinus 1e-12)
            astro.bpn[0, 1] shouldBe (-0.1136336653771609630e-7 plusOrMinus 1e-12)
            astro.bpn[1, 1] shouldBe (0.9999999995713154868 plusOrMinus 1e-12)
            astro.bpn[2, 1] shouldBe (-0.2928086230000000000e-4 plusOrMinus 1e-12)
            astro.bpn[0, 2] shouldBe (-0.1312227200895260194e-2 plusOrMinus 1e-12)
            astro.bpn[1, 2] shouldBe (0.2928082217872315680e-4 plusOrMinus 1e-12)
            astro.bpn[2, 2] shouldBe (0.9999991386008323373 plusOrMinus 1e-12)
            astro.along shouldBe (-0.5278008060295995734 plusOrMinus 1e-12)
            astro.xpl shouldBe (0.1133427418130752958e-5 plusOrMinus 1e-17)
            astro.ypl shouldBe (0.1453347595780646207e-5 plusOrMinus 1e-17)
            astro.sphi shouldBe (-0.9440115679003211329 plusOrMinus 1e-12)
            astro.cphi shouldBe (0.3299123514971474711 plusOrMinus 1e-12)
            astro.eral shouldBe (2.617608903970400427 plusOrMinus 1e-12)
            astro.refa shouldBe (0.2014187790000000000e-3 plusOrMinus 1e-17)
            astro.refb shouldBe (-0.2361408310000000000e-6 plusOrMinus 1e-18)
            astro.diurab shouldBeExactly 0.0
        }
        "eraSp00" {
            eraSp00(2400000.5, 52541.0) shouldBe (-0.6216698469981019309e-11 plusOrMinus 1e-12)
        }
        "eraObl06" {
            eraObl06(2400000.5, 54388.0) shouldBe (0.4090749229387258204 plusOrMinus 1e-14)
        }
        "eraPfw06" {
            val (gamb, phib, psib, epsa) = eraPfw06(2400000.5, 50123.9999)
            gamb shouldBe (-0.2243387670997995690e-5 plusOrMinus 1e-16)
            phib shouldBe (0.4091014602391312808 plusOrMinus 1e-12)
            psib shouldBe (-0.9501954178013031895e-3 plusOrMinus 1e-14)
            epsa shouldBe (0.4091014316587367491 plusOrMinus 1e-12)
        }
        "eraFal03" {
            eraFal03(0.80) shouldBe (5.132369751108684150 plusOrMinus 1e-12)
        }
        "eraFaf03" {
            eraFaf03(0.80) shouldBe (0.2597711366745499518 plusOrMinus 1e-12)
        }
        "eraFaom03" {
            eraFaom03(0.80) shouldBe (TAU - 5.973618440951302183 plusOrMinus 1e-12)
        }
        "eraFapa03" {
            eraFapa03(0.80) shouldBe (0.1950884762240000000e-1 plusOrMinus 1e-12)
        }
        "eraFame03" {
            eraFame03(0.80) shouldBe (5.417338184297289661 plusOrMinus 1e-12)
        }
        "eraFave03" {
            eraFave03(0.80) shouldBe (3.424900460533758000 plusOrMinus 1e-12)
        }
        "eraFae03" {
            eraFae03(0.80) shouldBe (1.744713738913081846 plusOrMinus 1e-12)
        }
        "eraFama03" {
            eraFama03(0.80) shouldBe (3.275506840277781492 plusOrMinus 1e-12)
        }
        "eraFaju03" {
            eraFaju03(0.80) shouldBe (5.275711665202481138 plusOrMinus 1e-12)
        }
        "eraFasa03" {
            eraFasa03(0.80) shouldBe (5.371574539440827046 plusOrMinus 1e-12)
        }
        "eraFaur03" {
            eraFaur03(0.80) shouldBe (5.180636450180413523 plusOrMinus 1e-12)
        }
        "eraFw2m" {
            val m = eraFw2m((-0.2243387670997992368e-5).rad, 0.4091014602391312982.rad, (-0.9501954178013015092e-3).rad, 0.4091014316587367472.rad)
            m[0, 0] shouldBe (0.9999995505176007047 plusOrMinus 1e-12)
            m[0, 1] shouldBe (0.8695404617348192957e-3 plusOrMinus 1e-12)
            m[0, 2] shouldBe (0.3779735201865582571e-3 plusOrMinus 1e-12)
            m[1, 0] shouldBe (-0.8695404723772016038e-3 plusOrMinus 1e-12)
            m[1, 1] shouldBe (0.9999996219496027161 plusOrMinus 1e-12)
            m[1, 2] shouldBe (-0.1361752496887100026e-6 plusOrMinus 1e-12)
            m[2, 0] shouldBe (-0.3779734957034082790e-3 plusOrMinus 1e-12)
            m[2, 1] shouldBe (-0.1924880848087615651e-6 plusOrMinus 1e-12)
            m[2, 2] shouldBe (0.9999999285679971958 plusOrMinus 1e-12)
        }
        "era00" {
            era00(2400000.0 + 54388.0, 0.5) shouldBe (0.4022837240028158102 plusOrMinus 1e-12)
        }
        "eraRefco" {
            val (refa, refb) = eraRefco(800.0, 10.0, 0.9, 0.4)
            refa shouldBe (0.2264949956241415009e-3 plusOrMinus 1e-15)
            refb shouldBe (-0.2598658261729343970e-6 plusOrMinus 1e-18)
        }
        "eraApcg" {
            val astrom = eraApcg(
                2456165.5, 0.401182685,
                Vector3D(0.901310875, -0.417402664, -0.180982288),
                Vector3D(0.00742727954, 0.0140507459, 0.00609045792),
                Vector3D(0.903358544, -0.415395237, -0.180084014),
            )

            astrom.pmt shouldBe (12.65133794027378508 plusOrMinus 1e-11)
            astrom.eb.x shouldBe (0.901310875 plusOrMinus 1e-12)
            astrom.eb.y shouldBe (-0.417402664 plusOrMinus 1e-12)
            astrom.eb.z shouldBe (-0.180982288 plusOrMinus 1e-12)
            astrom.eh.x shouldBe (0.8940025429324143045 plusOrMinus 1e-12)
            astrom.eh.y shouldBe (-0.4110930268679817955 plusOrMinus 1e-12)
            astrom.eh.z shouldBe (-0.1782189004872870264 plusOrMinus 1e-12)
            astrom.em shouldBe (1.010465295811013146 plusOrMinus 1e-12)
            astrom.v.x shouldBe (0.4289638913597693554e-4 plusOrMinus 1e-16)
            astrom.v.y shouldBe (0.8115034051581320575e-4 plusOrMinus 1e-16)
            astrom.v.z shouldBe (0.3517555136380563427e-4 plusOrMinus 1e-16)
            astrom.bm1 shouldBe (0.9999999951686012981 plusOrMinus 1e-12)
        }
        "eraEpv00" {
            val (h, b) = eraEpv00(2400000.5, 53411.52501161)
            h.position[0] shouldBe (-0.7757238809297706813 plusOrMinus 1e-14)
            h.position[1] shouldBe (0.5598052241363340596 plusOrMinus 1e-14)
            h.position[2] shouldBe (0.2426998466481686993 plusOrMinus 1e-14)

            h.velocity[0] shouldBe (-0.1091891824147313846e-1 plusOrMinus 1e-15)
            h.velocity[1] shouldBe (-0.1247187268440845008e-1 plusOrMinus 1e-15)
            h.velocity[2] shouldBe (-0.5407569418065039061e-2 plusOrMinus 1e-15)

            b.position[0] shouldBe (-0.7714104440491111971 plusOrMinus 1e-14)
            b.position[1] shouldBe (0.5598412061824171323 plusOrMinus 1e-14)
            b.position[2] shouldBe (0.2425996277722452400 plusOrMinus 1e-14)

            b.velocity[0] shouldBe (-0.1091874268116823295e-1 plusOrMinus 1e-15)
            b.velocity[1] shouldBe (-0.1246525461732861538e-1 plusOrMinus 1e-15)
            b.velocity[2] shouldBe (-0.5404773180966231279e-2 plusOrMinus 1e-15)
        }
        "eraApcg13" {
            val astrom = eraApcg13(2456165.5, 0.401182685)

            astrom.pmt shouldBe (12.65133794027378508 plusOrMinus 1e-11)
            astrom.eb.x shouldBe (0.9013108747340644755 plusOrMinus 1e-12)
            astrom.eb.y shouldBe (-0.4174026640406119957 plusOrMinus 1e-12)
            astrom.eb.z shouldBe (-0.1809822877867817771 plusOrMinus 1e-12)
            astrom.eh.x shouldBe (0.8940025429255499549 plusOrMinus 1e-12)
            astrom.eh.y shouldBe (-0.4110930268331896318 plusOrMinus 1e-12)
            astrom.eh.z shouldBe (-0.1782189006019749850 plusOrMinus 1e-12)
            astrom.em shouldBe (1.010465295964664178 plusOrMinus 1e-12)
            astrom.v.x shouldBe (0.4289638912941341125e-4 plusOrMinus 1e-16)
            astrom.v.y shouldBe (0.8115034032405042132e-4 plusOrMinus 1e-16)
            astrom.v.z shouldBe (0.3517555135536470279e-4 plusOrMinus 1e-16)
            astrom.bm1 shouldBe (0.9999999951686013142 plusOrMinus 1e-12)
        }
        "eraAe2hd" {
            val (ha, dec) = eraAe2hd(5.5.rad, 1.1.rad, 0.7.rad)

            ha shouldBe (0.5933291115507309663 plusOrMinus 1E-14)
            dec shouldBe (0.9613934761647817620 plusOrMinus 1E-14)
        }
        "eraTcbTdb" {
            val (whole, fraction) = eraTcbTdb(2453750.5, 0.893019599)
            whole shouldBe (2453750.5 plusOrMinus 1E-6)
            fraction shouldBe (0.8928551362746343397 plusOrMinus 1E-12)
        }
        "eraTcgTt" {
            val (whole, fraction) = eraTcgTt(2453750.5, 0.892862531)
            whole shouldBe (2453750.5 plusOrMinus 1E-6)
            fraction shouldBe (0.8928551387488816828 plusOrMinus 1E-12)
        }
        "eraTdbTcb" {
            val (whole, fraction) = eraTdbTcb(2453750.5, 0.892855137)
            whole shouldBe (2453750.5 plusOrMinus 1E-6)
            fraction shouldBe (0.8930195997253656716 plusOrMinus 1E-12)
        }
        "eraTtTcg" {
            val (whole, fraction) = eraTtTcg(2453750.5, 0.892482639)
            whole shouldBe (2453750.5 plusOrMinus 1E-6)
            fraction shouldBe (0.8924900312508587113 plusOrMinus 1E-12)
        }
        "eraDtDb" {
            val dt = eraDtDb(2448939.5, 0.123, 0.76543, 5.0123.rad, 5525.242.km, 3190.0.km)
            dt shouldBe (-0.1280368005936998991e-2 plusOrMinus 1E-15)
        }
        "eraPvtob" {
            val (p, v) = eraPvtob(2.0.rad, 0.5.rad, 3000.0, 1e-6.rad, (-0.5e-6).rad, 1e-8.rad, 5.0.rad)
            p[0] shouldBe (4225081.367071159207 plusOrMinus 1e-5)
            p[1] shouldBe (3681943.215856198144 plusOrMinus 1e-5)
            p[2] shouldBe (3041149.399241260785 plusOrMinus 1e-5)
            v[0] shouldBe (-268.4915389365998787 plusOrMinus 1e-5)
            v[1] shouldBe (308.0977983288903123 plusOrMinus 1e-5)
            v[2] shouldBe (0.0 plusOrMinus 1e-5)
        }
        "eraNut00a" {
            val (psi, eps) = eraNut00a(2400000.5, 53736.0)
            psi shouldBe (-0.9630909107115518431e-5 plusOrMinus 1e-13)
            eps shouldBe (0.4063239174001678710e-4 plusOrMinus 1e-13)
        }
        "eraNut06a" {
            val (psi, eps) = eraNut06a(2400000.5, 53736.0)
            psi shouldBe (-0.9630912025820308797e-5 plusOrMinus 1e-13)
            eps shouldBe (0.4063238496887249798e-4 plusOrMinus 1e-13)
        }
        "eraPnm06a" {
            val rbpn = eraPnm06a(2400000.5, 50123.9999)

            rbpn[0, 0] shouldBe (0.9999995832794205484 plusOrMinus 1e-12)
            rbpn[0, 1] shouldBe (0.8372382772630962111e-3 plusOrMinus 1e-14)
            rbpn[0, 2] shouldBe (0.3639684771140623099e-3 plusOrMinus 1e-14)
            rbpn[1, 0] shouldBe (-0.8372533744743683605e-3 plusOrMinus 1e-14)
            rbpn[1, 1] shouldBe (0.9999996486492861646 plusOrMinus 1e-12)
            rbpn[1, 2] shouldBe (0.4132905944611019498e-4 plusOrMinus 1e-14)
            rbpn[2, 0] shouldBe (-0.3639337469629464969e-3 plusOrMinus 1e-14)
            rbpn[2, 1] shouldBe (-0.4163377605910663999e-4 plusOrMinus 1e-14)
            rbpn[2, 2] shouldBe (0.9999999329094260057 plusOrMinus 1e-12)
        }
        "eraAb" {
            val pnat = Vector3D(-0.76321968546737951, -0.60869453983060384, -0.21676408580639883)
            val v = Vector3D(2.1044018893653786e-5, -8.9108923304429319e-5, -3.8633714797716569e-5)
            val ppr = eraAb(pnat, v, 0.99980921395708788.au, 0.99999999506209258)

            ppr[0] shouldBe (-0.7631631094219556269 plusOrMinus 1e-12)
            ppr[1] shouldBe (-0.6087553082505590832 plusOrMinus 1e-12)
            ppr[2] shouldBe (-0.2167926269368471279 plusOrMinus 1e-12)
        }
        "eraEect00" {
            val eect = eraEect00(2400000.5, 53736.0)
            eect shouldBe (0.2046085004885125264e-8 plusOrMinus 1e-20)
        }
        "eraEra00" {
            val era00 = eraEra00(2454388.0, 0.5)
            era00 shouldBe (0.4022837240028158102 plusOrMinus 1e-12)
        }
        "eraEe00" {
            val ee = eraEe00(2453736.0, 0.5, 0.4090789763356509900.rad, (-0.9630909107115582393e-5).rad)
            ee shouldBe (-0.8834193235367965479e-5 plusOrMinus 1e-18)
        }
        "eraGmst00" {
            val theta = eraGmst00(2453736.0, 0.5, 2453736.0, 0.5)
            theta shouldBe (1.754174972210740592 plusOrMinus 1e-12)
        }
        "eraGmst06" {
            val theta = eraGmst06(2453736.0, 0.5, 2453736.0, 0.5)
            theta shouldBe (1.754174971870091203 plusOrMinus 1e-12)
        }
        "eraEe06a" {
            val ee = eraEe06a(2453736.0, 0.5)
            ee shouldBe (-0.8834195072043790156e-5 plusOrMinus 1e-15)
        }
        "eraGst06a" {
            val theta = eraGst06a(2453736.0, 0.5, 2453736.0, 0.5)
            theta shouldBe (1.754166137675019159 plusOrMinus 1e-12)
        }
        "eraGst06" {
            val rnpb = Matrix3D(
                0.9999989440476103608, -0.1332881761240011518e-2, -0.5790767434730085097e-3,
                0.1332858254308954453e-2, 0.9999991109044505944, -0.4097782710401555759e-4,
                0.5791308472168153320e-3, 0.4020595661593994396e-4, 0.9999998314954572365,
            )

            val theta = eraGst06(2453736.0, 0.5, 2453736.0, 0.5, rnpb)
            theta shouldBe (1.754166138018167568 plusOrMinus 1e-12)
        }
        "eraS06" {
            val s = eraS06(2400000.5, 53736.0, 0.5791308486706011000e-3, 0.4020579816732961219e-4)
            s shouldBe (-0.1220032213076463117e-7 plusOrMinus 1e-18)
        }
        "eraS06a" {
            val s = eraS06a(2400000.5, 52541.0)
            s shouldBe (-0.1340680437291812383e-7 plusOrMinus 1e-18)
        }
        "eraEors" {
            val rnpb = Matrix3D(
                0.9999989440476103608, -0.1332881761240011518e-2, -0.5790767434730085097e-3,
                0.1332858254308954453e-2, 0.9999991109044505944, -0.4097782710401555759e-4,
                0.5791308472168153320e-3, 0.4020595661593994396e-4, 0.9999998314954572365,
            )

            val eo = eraEors(rnpb, (-0.1220040848472271978e-7).rad)
            eo shouldBe (-0.1332882715130744606e-2 plusOrMinus 1e-14)
        }
        "eraGst00a" {
            val theta = eraGst00a(2453736.0, 0.5, 2453736.0, 0.5)
            theta shouldBe (1.754166138018281369 plusOrMinus 1e-12)
        }
        "eraGst00b" {
            val theta = eraGst00b(2453736.0, 0.5)
            theta shouldBe (1.754166136510680589 plusOrMinus 1e-12)
        }
        "eraEe00a" {
            val ee = eraEe00a(2453736.0, 0.5)
            ee shouldBe (-0.8834192459222588227e-5 plusOrMinus 1e-18)
        }
        "eraEe00b" {
            val ee = eraEe00b(2453736.0, 0.5)
            ee shouldBe (-0.8835700060003032831e-5 plusOrMinus 1e-18)
        }
        "eraPr00" {
            val (dpsipr, depspr) = eraPr00(2453736.0, 0.5)
            dpsipr shouldBe (-0.8716465172668347629e-7 plusOrMinus 1e-22)
            depspr shouldBe (-0.7342018386722813087e-8 plusOrMinus 1e-22)
        }
        "eraObl80" {
            val eps0 = eraObl80(2454388.0, 0.5)
            eps0 shouldBe (0.4090751347643816218 plusOrMinus 1e-14)
        }
        "eraNut00b" {
            val (dpsi, deps) = eraNut00b(2453736.0, 0.5)
            dpsi shouldBe (-0.9632552291148362783e-5 plusOrMinus 1e-13)
            deps shouldBe (0.4063197106621159367e-4 plusOrMinus 1e-13)
        }
        "eraC2tcio" {
            val rc2i = Matrix3D(
                0.9999998323037164738, 0.5581526271714303683e-9, -0.5791308477073443903e-3,
                -0.2384266227524722273e-7, 0.9999999991917404296, -0.4020594955030704125e-4,
                0.5791308472168153320e-3, 0.4020595661593994396e-4, 0.9999998314954572365,
            )

            val rpom = Matrix3D(
                0.9999999999999674705, -0.1367174580728847031e-10, 0.2550602379999972723e-6,
                0.1414624947957029721e-10, 0.9999999999982694954, -0.1860359246998866338e-5,
                -0.2550602379741215275e-6, 0.1860359247002413923e-5, 0.9999999999982369658,
            )

            val rc2t = eraC2tcio(rc2i, 1.75283325530307.rad, rpom)

            rc2t[0] shouldBe (-0.1810332128307110439 plusOrMinus 1e-12)
            rc2t[1] shouldBe (0.9834769806938470149 plusOrMinus 1e-12)
            rc2t[2] shouldBe (0.6555535638685466874e-4 plusOrMinus 1e-12)
            rc2t[3] shouldBe (-0.9834768134135996657 plusOrMinus 1e-12)
            rc2t[4] shouldBe (-0.1810332203649448367 plusOrMinus 1e-12)
            rc2t[5] shouldBe (0.5749801116141106528e-3 plusOrMinus 1e-12)
            rc2t[6] shouldBe (0.5773474014081407076e-3 plusOrMinus 1e-12)
            rc2t[7] shouldBe (0.3961832391772658944e-4 plusOrMinus 1e-12)
            rc2t[8] shouldBe (0.9999998325501691969 plusOrMinus 1e-12)
        }
        "eraEcm06" {
            val rm = eraEcm06(2456165.5, 0.401182685)

            rm[0, 0] shouldBe (0.9999952427708701137 plusOrMinus 1e-14)
            rm[0, 1] shouldBe (-0.2829062057663042347e-2 plusOrMinus 1e-14)
            rm[0, 2] shouldBe (-0.1229163741100017629e-2 plusOrMinus 1e-14)
            rm[1, 0] shouldBe (0.3084546876908653562e-2 plusOrMinus 1e-14)
            rm[1, 1] shouldBe (0.9174891871550392514 plusOrMinus 1e-14)
            rm[1, 2] shouldBe (0.3977487611849338124 plusOrMinus 1e-14)
            rm[2, 0] shouldBe (0.2488512951527405928e-5 plusOrMinus 1e-14)
            rm[2, 1] shouldBe (-0.3977506604161195467 plusOrMinus 1e-14)
            rm[2, 2] shouldBe (0.9174935488232863071 plusOrMinus 1e-14)
        }
        "eraPmat06" {
            val rbp = eraPmat06(2400000.5, 50123.9999)

            rbp[0, 0] shouldBe (0.9999995505176007047 plusOrMinus 1e-12)
            rbp[0, 1] shouldBe (0.8695404617348208406e-3 plusOrMinus 1e-14)
            rbp[0, 2] shouldBe (0.3779735201865589104e-3 plusOrMinus 1e-14)
            rbp[1, 0] shouldBe (-0.8695404723772031414e-3 plusOrMinus 1e-14)
            rbp[1, 1] shouldBe (0.9999996219496027161 plusOrMinus 1e-12)
            rbp[1, 2] shouldBe (-0.1361752497080270143e-6 plusOrMinus 1e-14)
            rbp[2, 0] shouldBe (-0.3779734957034089490e-3 plusOrMinus 1e-14)
            rbp[2, 1] shouldBe (-0.1924880847894457113e-6 plusOrMinus 1e-14)
            rbp[2, 2] shouldBe (0.9999999285679971958 plusOrMinus 1e-12)
        }
        "eraP06e" {
            val e = eraP06e(2452541.0, 0.5)

            e.eps0 shouldBe (0.4090926006005828715 plusOrMinus 1e-14)
            e.psia shouldBe (0.6664369630191613431e-3 plusOrMinus 1e-14)
            e.oma shouldBe (0.4090925973783255982 plusOrMinus 1e-14)
            e.bpa shouldBe (0.5561149371265209445e-6 plusOrMinus 1e-14)
            e.bqa shouldBe (-0.6191517193290621270e-5 plusOrMinus 1e-14)
            e.pia shouldBe (0.6216441751884382923e-5 plusOrMinus 1e-14)
            e.bpia shouldBe (3.052014180023779882 plusOrMinus 1e-14)
            e.epsa shouldBe (0.4090864054922431688 plusOrMinus 1e-14)
            e.chia shouldBe (0.1387703379530915364e-5 plusOrMinus 1e-14)
            e.za shouldBe (0.2921789846651790546e-3 plusOrMinus 1e-14)
            e.zetaa shouldBe (0.3178773290332009310e-3 plusOrMinus 1e-14)
            e.thetaa shouldBe (0.2650932701657497181e-3 plusOrMinus 1e-14)
            e.pa shouldBe (0.6651637681381016288e-3 plusOrMinus 1e-14)
            e.gam shouldBe (0.1398077115963754987e-5 plusOrMinus 1e-14)
            e.phi shouldBe (0.4090864090837462602 plusOrMinus 1e-14)
            e.psi shouldBe (0.6664464807480920325e-3 plusOrMinus 1e-14)
        }
        "eraNumat" {
            val rmatn = eraNumat(0.4090789763356509900.rad, (-0.9630909107115582393e-5).rad, 0.4063239174001678826e-4.rad)

            rmatn[0, 0] shouldBe (0.9999999999536227949 plusOrMinus 1e-12)
            rmatn[0, 1] shouldBe (0.8836239320236250577e-5 plusOrMinus 1e-12)
            rmatn[0, 2] shouldBe (0.3830833447458251908e-5 plusOrMinus 1e-12)
            rmatn[1, 0] shouldBe (-0.8836083657016688588e-5 plusOrMinus 1e-12)
            rmatn[1, 1] shouldBe (0.9999999991354654959 plusOrMinus 1e-12)
            rmatn[1, 2] shouldBe (-0.4063240865361857698e-4 plusOrMinus 1e-12)
            rmatn[2, 0] shouldBe (-0.3831192481833385226e-5 plusOrMinus 1e-12)
            rmatn[2, 1] shouldBe (0.4063237480216934159e-4 plusOrMinus 1e-12)
            rmatn[2, 2] shouldBe (0.9999999991671660407 plusOrMinus 1e-12)
        }
        "eraNum06a" {
            val rmatn = eraNum06a(2453736.0, 0.5)

            rmatn[0, 0] shouldBe (0.9999999999536227668 plusOrMinus 1e-12)
            rmatn[0, 1] shouldBe (0.8836241998111535233e-5 plusOrMinus 1e-12)
            rmatn[0, 2] shouldBe (0.3830834608415287707e-5 plusOrMinus 1e-12)
            rmatn[1, 0] shouldBe (-0.8836086334870740138e-5 plusOrMinus 1e-12)
            rmatn[1, 1] shouldBe (0.9999999991354657474 plusOrMinus 1e-12)
            rmatn[1, 2] shouldBe (-0.4063240188248455065e-4 plusOrMinus 1e-12)
            rmatn[2, 0] shouldBe (-0.3831193642839398128e-5 plusOrMinus 1e-12)
            rmatn[2, 1] shouldBe (0.4063236803101479770e-4 plusOrMinus 1e-12)
            rmatn[2, 2] shouldBe (0.9999999991671663114 plusOrMinus 1e-12)
        }
        "eraC2teqx" {
            val rbpn = Matrix3D(
                0.9999989440476103608, -0.1332881761240011518e-2, -0.5790767434730085097e-3,
                0.1332858254308954453e-2, 0.9999991109044505944, -0.4097782710401555759e-4,
                0.5791308472168153320e-3, 0.4020595661593994396e-4, 0.9999998314954572365,
            )

            val rpom = Matrix3D(
                0.9999999999999674705, -0.1367174580728847031e-10, 0.2550602379999972723e-6,
                0.1414624947957029721e-10, 0.9999999999982694954, -0.1860359246998866338e-5,
                -0.2550602379741215275e-6, 0.1860359247002413923e-5, 0.9999999999982369658,
            )

            val rc2t = eraC2teqx(rbpn, 1.754166138040730516.rad, rpom)

            rc2t[0, 0] shouldBe (-0.1810332128528685730 plusOrMinus 1e-12)
            rc2t[0, 1] shouldBe (0.9834769806897685071 plusOrMinus 1e-12)
            rc2t[0, 2] shouldBe (0.6555535639982634449e-4 plusOrMinus 1e-12)

            rc2t[1, 0] shouldBe (-0.9834768134095211257 plusOrMinus 1e-12)
            rc2t[1, 1] shouldBe (-0.1810332203871023800 plusOrMinus 1e-12)
            rc2t[1, 2] shouldBe (0.5749801116126438962e-3 plusOrMinus 1e-12)

            rc2t[2, 0] shouldBe (0.5773474014081539467e-3 plusOrMinus 1e-12)
            rc2t[2, 1] shouldBe (0.3961832391768640871e-4 plusOrMinus 1e-12)
            rc2t[2, 2] shouldBe (0.9999998325501691969 plusOrMinus 1e-12)
        }
        "eraTpors" {
            val (a, b) = eraTpors((-0.03).rad, 0.07.rad, 1.3.rad, 1.5.rad).shouldNotBeNull()

            a shouldBe (4.004971075806584490 plusOrMinus 1e-13)
            b shouldBe (1.565084088476417917 plusOrMinus 1e-13)
        }
        "eraTpsts" {
            val (ra, dec) = eraTpsts((-0.03).rad, 0.07.rad, 2.3.rad, 1.5.rad)

            ra shouldBe (0.7596127167359629775 plusOrMinus 1e-14)
            dec shouldBe (1.540864645109263028 plusOrMinus 1e-13)
        }
        "eraTporv" {
            val s = CartesianCoordinate.of(1.3.rad, 1.5.rad, 1.0.au)
            val v = doubleArrayOf(s.x, s.y, s.z)

            val (a, b, c) = eraTporv((-0.03).rad, 0.07.rad, v).shouldNotBeNull()

            a shouldBe (-0.003712211763801968173 plusOrMinus 1e-16)
            b shouldBe (-0.004341519956299836813 plusOrMinus 1e-16)
            c shouldBe (0.9999836852110587012 plusOrMinus 1e-14)
        }
        "eraTpstv" {
            val s = CartesianCoordinate.of(2.3.rad, 1.5.rad, 1.0.au)
            val v = doubleArrayOf(s.x, s.y, s.z)

            val (a, b, c) = eraTpstv((-0.03).rad, 0.07.rad, v).shouldNotBeNull()

            a shouldBe (0.02170030454907376677 plusOrMinus 1e-15)
            b shouldBe (0.02060909590535367447 plusOrMinus 1e-15)
            c shouldBe (0.999552080658352380 plusOrMinus 1e-14)
        }
        "eraTpxes" {
            val (xi, eta, j) = eraTpxes(1.3.rad, 1.55.rad, 2.3.rad, 1.5.rad)

            xi shouldBe (-0.01753200983236980595 plusOrMinus 1e-15)
            eta shouldBe (0.05962940005778712891 plusOrMinus 1e-15)
            j shouldBeExactly 0
        }
        "eraTpxev" {
            val s = CartesianCoordinate.of(1.3.rad, 1.55.rad, 1.0.au)
            val v = doubleArrayOf(s.x, s.y, s.z)

            val s0 = CartesianCoordinate.of(2.3.rad, 1.5.rad, 1.0.au)
            val v0 = doubleArrayOf(s0.x, s0.y, s0.z)

            val (xi, eta, j) = eraTpxev(v, v0)

            xi shouldBe (-0.01753200983236980595 plusOrMinus 1e-15)
            eta shouldBe (0.05962940005778712891 plusOrMinus 1e-15)
            j shouldBeExactly 0
        }
        "eraPb06" {
            val (zeta, z, theta) = eraPb06(2400000.5, 50123.9999)

            zeta shouldBe (-0.5092634016326478238e-3 plusOrMinus 1e-12)
            z shouldBe (-0.3602772060566044413e-3 plusOrMinus 1e-12)
            theta shouldBe (-0.3779735537167811177e-3 plusOrMinus 1e-12)
        }
        "eraJd2Cal" {
            val (y, m, d, f) = eraJd2Cal(2400000.5, 50123.9999)
            y shouldBeExactly 1996
            m shouldBeExactly 2
            d shouldBeExactly 10
            f shouldBe (0.9999 plusOrMinus 1e-7)
        }
        "eraCal2Jd" {
            eraCal2Jd(2003, 6, 1) shouldBeExactly 52791.0
        }
        "eraDat" {
            eraDat(2003, 6, 1, 0.0) shouldBeExactly 32.0
            eraDat(2008, 1, 17, 0.0) shouldBeExactly 33.0
            eraDat(2017, 9, 1, 0.0) shouldBeExactly 37.0
        }
        "eraUt1Utc" {
            val (u1, u2) = eraUt1Utc(2453750.5, 0.892104561, 0.3341)
            u1 shouldBeExactly 2453750.5
            u2 shouldBe (0.8921006941018518519 plusOrMinus 1e-12)
        }
        "eraUtcTai" {
            val (u1, u2) = eraUtcTai(2453750.5, 0.892100694)
            u1 shouldBeExactly 2453750.5
            u2 shouldBe (0.8924826384444444444 plusOrMinus 1e-12)
        }
        "eraTaiUt1" {
            val (u1, u2) = eraTaiUt1(2453750.5, 0.892482639, -32.6659)
            u1 shouldBeExactly 2453750.5
            u2 shouldBe (0.8921045614537037037 plusOrMinus 1e-12)
        }
        "eraUtcUt1" {
            val (u1, u2) = eraUtcUt1(2453750.5, 0.892100694, 0.3341)
            u1 shouldBeExactly 2453750.5
            u2 shouldBe (0.8921045608981481481 plusOrMinus 1e-12)
        }
        "eraTaiUtc" {
            val (u1, u2) = eraTaiUtc(2453750.5, 0.892482639)
            u1 shouldBeExactly 2453750.5
            u2 shouldBe (0.8921006945555555556 plusOrMinus 1e-12)
        }
        "eraTaiTt" {
            val (u1, u2) = eraTaiTt(2453750.5, 0.892482639)
            u1 shouldBeExactly 2453750.5
            u2 shouldBe (0.892855139 plusOrMinus 1e-12)
        }
        "eraTtTai" {
            val (u1, u2) = eraTtTai(2453750.5, 0.892482639)
            u1 shouldBeExactly 2453750.5
            u2 shouldBe (0.892110139 plusOrMinus 1e-12)
        }
        "eraTtTdb" {
            val (u1, u2) = eraTtTdb(2453750.5, 0.892855139, -0.000201)
            u1 shouldBeExactly 2453750.5
            u2 shouldBe (0.8928551366736111111 plusOrMinus 1e-12)
        }
        "eraTdbTt" {
            val (u1, u2) = eraTdbTt(2453750.5, 0.892855137, -0.000201)
            u1 shouldBeExactly 2453750.5
            u2 shouldBe (0.8928551393263888889 plusOrMinus 1e-12)
        }
        "eraUt1Tai" {
            val (u1, u2) = eraUt1Tai(2453750.5, 0.892104561, -32.6659)
            u1 shouldBeExactly 2453750.5
            u2 shouldBe (0.8924826385462962963 plusOrMinus 1e-12)
        }
        "eraTtUt1" {
            val (u1, u2) = eraTtUt1(2453750.5, 0.892855139, 64.8499)
            u1 shouldBeExactly 2453750.5
            u2 shouldBe (0.8921045614537037037 plusOrMinus 1e-12)
        }
        "eraBp00" {
            val (rb, rp, rbp) = eraBp00(2400000.5, 50123.9999)

            rb[0, 0] shouldBe (0.9999999999999942498 plusOrMinus 1e-12)
            rb[0, 1] shouldBe (-0.7078279744199196626e-7 plusOrMinus 1e-16)
            rb[0, 2] shouldBe (0.8056217146976134152e-7 plusOrMinus 1e-16)
            rb[1, 0] shouldBe (0.7078279477857337206e-7 plusOrMinus 1e-16)
            rb[1, 1] shouldBe (0.9999999999999969484 plusOrMinus 1e-12)
            rb[1, 2] shouldBe (0.3306041454222136517e-7 plusOrMinus 1e-16)
            rb[2, 0] shouldBe (-0.8056217380986972157e-7 plusOrMinus 1e-16)
            rb[2, 1] shouldBe (-0.3306040883980552500e-7 plusOrMinus 1e-16)
            rb[2, 2] shouldBe (0.9999999999999962084 plusOrMinus 1e-12)
            rp[0, 0] shouldBe (0.9999995504864048241 plusOrMinus 1e-12)
            rp[0, 1] shouldBe (0.8696113836207084411e-3 plusOrMinus 1e-14)
            rp[0, 2] shouldBe (0.3778928813389333402e-3 plusOrMinus 1e-14)
            rp[1, 0] shouldBe (-0.8696113818227265968e-3 plusOrMinus 1e-14)
            rp[1, 1] shouldBe (0.9999996218879365258 plusOrMinus 1e-12)
            rp[1, 2] shouldBe (-0.1690679263009242066e-6 plusOrMinus 1e-14)
            rp[2, 0] shouldBe (-0.3778928854764695214e-3 plusOrMinus 1e-14)
            rp[2, 1] shouldBe (-0.1595521004195286491e-6 plusOrMinus 1e-14)
            rp[2, 2] shouldBe (0.9999999285984682756 plusOrMinus 1e-12)
            rbp[0, 0] shouldBe (0.9999995505175087260 plusOrMinus 1e-12)
            rbp[0, 1] shouldBe (0.8695405883617884705e-3 plusOrMinus 1e-14)
            rbp[0, 2] shouldBe (0.3779734722239007105e-3 plusOrMinus 1e-14)
            rbp[1, 0] shouldBe (-0.8695405990410863719e-3 plusOrMinus 1e-14)
            rbp[1, 1] shouldBe (0.9999996219494925900 plusOrMinus 1e-12)
            rbp[1, 2] shouldBe (-0.1360775820404982209e-6 plusOrMinus 1e-14)
            rbp[2, 0] shouldBe (-0.3779734476558184991e-3 plusOrMinus 1e-14)
            rbp[2, 1] shouldBe (-0.1925857585832024058e-6 plusOrMinus 1e-14)
            rbp[2, 2] shouldBe (0.9999999285680153377 plusOrMinus 1e-12)
        }
        "eraPn00" {
            val (_, _, epsa, rb, rp, rbp, rn, rbpn) = eraPn00(2400000.5, 53736.0, -0.9632552291149335877e-5, 0.4063197106621141414e-4)

            epsa shouldBe (0.4090791789404229916 plusOrMinus 1e-12)

            rb[0, 0] shouldBe (0.9999999999999942498 plusOrMinus 1e-12)
            rb[0, 1] shouldBe (-0.7078279744199196626e-7 plusOrMinus 1e-18)
            rb[0, 2] shouldBe (0.8056217146976134152e-7 plusOrMinus 1e-18)

            rb[1, 0] shouldBe (0.7078279477857337206e-7 plusOrMinus 1e-18)
            rb[1, 1] shouldBe (0.9999999999999969484 plusOrMinus 1e-12)
            rb[1, 2] shouldBe (0.3306041454222136517e-7 plusOrMinus 1e-18)

            rb[2, 0] shouldBe (-0.8056217380986972157e-7 plusOrMinus 1e-18)
            rb[2, 1] shouldBe (-0.3306040883980552500e-7 plusOrMinus 1e-18)
            rb[2, 2] shouldBe (0.9999999999999962084 plusOrMinus 1e-12)

            rp[0, 0] shouldBe (0.9999989300532289018 plusOrMinus 1e-12)
            rp[0, 1] shouldBe (-0.1341647226791824349e-2 plusOrMinus 1e-14)
            rp[0, 2] shouldBe (-0.5829880927190296547e-3 plusOrMinus 1e-14)

            rp[1, 0] shouldBe (0.1341647231069759008e-2 plusOrMinus 1e-14)
            rp[1, 1] shouldBe (0.9999990999908750433 plusOrMinus 1e-12)
            rp[1, 2] shouldBe (-0.3837444441583715468e-6 plusOrMinus 1e-14)

            rp[2, 0] shouldBe (0.5829880828740957684e-3 plusOrMinus 1e-14)
            rp[2, 1] shouldBe (-0.3984203267708834759e-6 plusOrMinus 1e-14)
            rp[2, 2] shouldBe (0.9999998300623538046 plusOrMinus 1e-12)

            rbp[0, 0] shouldBe (0.9999989300052243993 plusOrMinus 1e-12)
            rbp[0, 1] shouldBe (-0.1341717990239703727e-2 plusOrMinus 1e-14)
            rbp[0, 2] shouldBe (-0.5829075749891684053e-3 plusOrMinus 1e-14)

            rbp[1, 0] shouldBe (0.1341718013831739992e-2 plusOrMinus 1e-14)
            rbp[1, 1] shouldBe (0.9999990998959191343 plusOrMinus 1e-12)
            rbp[1, 2] shouldBe (-0.3505759733565421170e-6 plusOrMinus 1e-14)

            rbp[2, 0] shouldBe (0.5829075206857717883e-3 plusOrMinus 1e-14)
            rbp[2, 1] shouldBe (-0.4315219955198608970e-6 plusOrMinus 1e-14)
            rbp[2, 2] shouldBe (0.9999998301093036269 plusOrMinus 1e-12)

            rn[0, 0] shouldBe (0.9999999999536069682 plusOrMinus 1e-12)
            rn[0, 1] shouldBe (0.8837746144872140812e-5 plusOrMinus 1e-16)
            rn[0, 2] shouldBe (0.3831488838252590008e-5 plusOrMinus 1e-16)

            rn[1, 0] shouldBe (-0.8837590456633197506e-5 plusOrMinus 1e-16)
            rn[1, 1] shouldBe (0.9999999991354692733 plusOrMinus 1e-12)
            rn[1, 2] shouldBe (-0.4063198798559573702e-4 plusOrMinus 1e-16)

            rn[2, 0] shouldBe (-0.3831847930135328368e-5 plusOrMinus 1e-16)
            rn[2, 1] shouldBe (0.4063195412258150427e-4 plusOrMinus 1e-16)
            rn[2, 2] shouldBe (0.9999999991671806225 plusOrMinus 1e-12)

            rbpn[0, 0] shouldBe (0.9999989440499982806 plusOrMinus 1e-12)
            rbpn[0, 1] shouldBe (-0.1332880253640848301e-2 plusOrMinus 1e-14)
            rbpn[0, 2] shouldBe (-0.5790760898731087295e-3 plusOrMinus 1e-14)

            rbpn[1, 0] shouldBe (0.1332856746979948745e-2 plusOrMinus 1e-14)
            rbpn[1, 1] shouldBe (0.9999991109064768883 plusOrMinus 1e-12)
            rbpn[1, 2] shouldBe (-0.4097740555723063806e-4 plusOrMinus 1e-14)

            rbpn[2, 0] shouldBe (0.5791301929950205000e-3 plusOrMinus 1e-14)
            rbpn[2, 1] shouldBe (0.4020553681373702931e-4 plusOrMinus 1e-14)
            rbpn[2, 2] shouldBe (0.9999998314958529887 plusOrMinus 1e-12)
        }
        "eraC2tpe" {
            val rc2t =
                eraC2tpe(2400000.5, 53736.0, 2400000.5, 53736.0, -0.9630909107115582393e-5, 0.4090789763356509900, 2.55060238e-7, 1.860359247e-6)

            rc2t[0, 0] shouldBe (-0.1813677995763029394 plusOrMinus 1e-12)
            rc2t[0, 1] shouldBe (0.9023482206891683275 plusOrMinus 1e-12)
            rc2t[0, 2] shouldBe (-0.3909902938641085751 plusOrMinus 1e-12)

            rc2t[1, 0] shouldBe (-0.9834147641476804807 plusOrMinus 1e-12)
            rc2t[1, 1] shouldBe (-0.1659883635434995121 plusOrMinus 1e-12)
            rc2t[1, 2] shouldBe (0.7309763898042819705e-1 plusOrMinus 1e-12)

            rc2t[2, 0] shouldBe (0.1059685430673215247e-2 plusOrMinus 1e-12)
            rc2t[2, 1] shouldBe (0.3977631855605078674 plusOrMinus 1e-12)
            rc2t[2, 2] shouldBe (0.9174875068792735362 plusOrMinus 1e-12)
        }
        "eraS00" {
            val s = eraS00(2400000.5, 53736.0, 0.5791308486706011000e-3, 0.4020579816732961219e-4)
            s shouldBe (-0.1220036263270905693e-7 plusOrMinus 1e-18)
        }
        "eraS00b" {
            val s = eraS00b(2400000.5, 52541.0)
            s shouldBe (-0.1340695782951026584e-7 plusOrMinus 1e-18)
        }
        "eraS00a" {
            val s = eraS00a(2400000.5, 52541.0)
            s shouldBe (-0.1340684448919163584e-7 plusOrMinus 1e-18)
        }
        "eraApco13" {
            val (astrom, eo) = eraApco13(
                2456384.5, 0.969254051, 0.1550675,
                -0.527800806, -1.2345856, 2738.0,
                2.47230737e-7, 1.82640464e-6,
                731.0, 12.8, 0.59, 0.55
            )

            astrom.pmt shouldBe (13.25248468622475727 plusOrMinus 1e-11)
            astrom.eb.x shouldBe (-0.9741827107320875162 plusOrMinus 1e-12)
            astrom.eb.y shouldBe (-0.2115130190489716682 plusOrMinus 1e-12)
            astrom.eb.z shouldBe (-0.09179840189496755339 plusOrMinus 1e-12)
            astrom.eh.x shouldBe (-0.9736425572586935247 plusOrMinus 1e-12)
            astrom.eh.y shouldBe (-0.2092452121603336166 plusOrMinus 1e-12)
            astrom.eh.z shouldBe (-0.09075578153885665295 plusOrMinus 1e-12)
            astrom.em shouldBe (0.9998233240913898141 plusOrMinus 1e-12)
            astrom.v.x shouldBe (0.2078704994520489246e-4 plusOrMinus 1e-16)
            astrom.v.y shouldBe (-0.8955360133238868938e-4 plusOrMinus 1e-16)
            astrom.v.z shouldBe (-0.3863338993055887398e-4 plusOrMinus 1e-16)
            astrom.bm1 shouldBe (0.9999999950277561004 plusOrMinus 1e-12)
            astrom.bpn[0, 0] shouldBe (0.9999991390295147999 plusOrMinus 1e-12)
            astrom.bpn[1, 0] shouldBe (0.4978650075315529277e-7 plusOrMinus 1e-12)
            astrom.bpn[2, 0] shouldBe (0.001312227200850293372 plusOrMinus 1e-12)
            astrom.bpn[0, 1] shouldBe (-0.1136336652812486604e-7 plusOrMinus 1e-12)
            astrom.bpn[1, 1] shouldBe (0.9999999995713154865 plusOrMinus 1e-12)
            astrom.bpn[2, 1] shouldBe (-0.2928086230975367296e-4 plusOrMinus 1e-12)
            astrom.bpn[0, 2] shouldBe (-0.001312227201745553566 plusOrMinus 1e-12)
            astrom.bpn[1, 2] shouldBe (0.2928082218847679162e-4 plusOrMinus 1e-12)
            astrom.bpn[2, 2] shouldBe (0.9999991386008312212 plusOrMinus 1e-12)
            astrom.along shouldBe (-0.5278008060295995733 plusOrMinus 1e-12)
            astrom.xpl shouldBe (0.1133427418130752958e-5 plusOrMinus 1e-17)
            astrom.ypl shouldBe (0.1453347595780646207e-5 plusOrMinus 1e-17)
            astrom.sphi shouldBe (-0.9440115679003211329 plusOrMinus 1e-12)
            astrom.cphi shouldBe (0.3299123514971474711 plusOrMinus 1e-12)
            astrom.diurab shouldBeExactly 0.0
            astrom.eral shouldBe (2.617608909189664000 plusOrMinus 1e-12)
            astrom.refa shouldBe (0.2014187785940396921e-3 plusOrMinus 1e-15)
            astrom.refb shouldBe (-0.2361408314943696227e-6 plusOrMinus 1e-18)
            eo shouldBe (-0.003020548354802412839 plusOrMinus 1e-14)
        }
        "eraPmpx" {
            val pco = eraPmpx(1.234, 0.789, 1e-5, -2e-5, 1e-2.arcsec, 10.0.kms, 8.75, Vector3D(0.9, 0.4, 0.1))
            pco[0] shouldBe (0.2328137623960308438 plusOrMinus 1e-12)
            pco[1] shouldBe (0.6651097085397855328 plusOrMinus 1e-12)
            pco[2] shouldBe (0.7095257765896359837 plusOrMinus 1e-12)
        }
        "eraAtciq" {
            val (astrom) = eraApci13(2456165.5, 0.401182685)
            val (ri, di) = eraAtciq(2.71, 0.174, 1e-5, 5e-6, 0.1.arcsec, 55.0.kms, astrom)
            ri shouldBe (2.710121572968696744 plusOrMinus 1e-12)
            di shouldBe (0.1729371367219539137 plusOrMinus 1e-12)
        }
        "eraAtciqz" {
            val (astrom) = eraApci13(2456165.5, 0.401182685)
            val (ri, di) = eraAtciqz(2.71, 0.174, astrom)
            ri shouldBe (2.709994899247256984 plusOrMinus 1e-12)
            di shouldBe (0.1728740720984931891 plusOrMinus 1e-12)
        }
        "eraAtci13" {
            val (ri, di, eo) = eraAtci13(2.71, 0.174, 1e-5, 5e-6, 0.1.arcsec, 55.0.kms, 2456165.5, 0.401182685)
            ri shouldBe (2.710121572968696744 plusOrMinus 1e-12)
            di shouldBe (0.1729371367219539137 plusOrMinus 1e-12)
            eo shouldBe (-0.002900618712657375647 plusOrMinus 1e-14)
        }
        "eraApci13" {
            val (astrom, eo) = eraApci13(2456165.5, 0.401182685)

            astrom.pmt shouldBe (12.65133794027378508 plusOrMinus 1e-11)
            astrom.eb[0] shouldBe (0.9013108747340644755 plusOrMinus 1e-12)
            astrom.eb[1] shouldBe (-0.4174026640406119957 plusOrMinus 1e-12)
            astrom.eb[2] shouldBe (-0.1809822877867817771 plusOrMinus 1e-12)
            astrom.eh[0] shouldBe (0.8940025429255499549 plusOrMinus 1e-12)
            astrom.eh[1] shouldBe (-0.4110930268331896318 plusOrMinus 1e-12)
            astrom.eh[2] shouldBe (-0.1782189006019749850 plusOrMinus 1e-12)
            astrom.em shouldBe (1.010465295964664178 plusOrMinus 1e-12)
            astrom.v[0] shouldBe (0.4289638912941341125e-4 plusOrMinus 1e-16)
            astrom.v[1] shouldBe (0.8115034032405042132e-4 plusOrMinus 1e-16)
            astrom.v[2] shouldBe (0.3517555135536470279e-4 plusOrMinus 1e-16)
            astrom.bm1 shouldBe (0.9999999951686013142 plusOrMinus 1e-12)
            astrom.bpn[0, 0] shouldBe (0.9999992060376761710 plusOrMinus 1e-12)
            astrom.bpn[1, 0] shouldBe (0.4124244860106037157e-7 plusOrMinus 1e-12)
            astrom.bpn[2, 0] shouldBe (0.1260128571051709670e-2 plusOrMinus 1e-12)
            astrom.bpn[0, 1] shouldBe (-0.1282291987222130690e-7 plusOrMinus 1e-12)
            astrom.bpn[1, 1] shouldBe (0.9999999997456835325 plusOrMinus 1e-12)
            astrom.bpn[2, 1] shouldBe (-0.2255288829420524935e-4 plusOrMinus 1e-12)
            astrom.bpn[0, 2] shouldBe (-0.1260128571661374559e-2 plusOrMinus 1e-12)
            astrom.bpn[1, 2] shouldBe (0.2255285422953395494e-4 plusOrMinus 1e-12)
            astrom.bpn[2, 2] shouldBe (0.9999992057833604343 plusOrMinus 1e-12)
            eo shouldBe (-0.2900618712657375647e-2 plusOrMinus 1e-12)
        }
        "eraLdsun" {
            val p = Vector3D(-0.763276255, -0.608633767, -0.216735543)
            val e = Vector3D(-0.973644023, -0.20925523, -0.0907169552)
            val p1 = eraLdsun(p, e, 0.999809214)
            p1[0] shouldBe (-0.7632762580731413169 plusOrMinus 1e-12)
            p1[1] shouldBe (-0.6086337635262647900 plusOrMinus 1e-12)
            p1[2] shouldBe (-0.2167355419322321302 plusOrMinus 1e-12)
        }
        "eraLd" {
            val p = Vector3D(-0.763276255, -0.608633767, -0.216735543)
            val q = Vector3D(-0.763276255, -0.608633767, -0.216735543)
            val e = Vector3D(0.76700421, 0.605629598, 0.211937094)
            val p1 = eraLd(0.00028574, p, q, e, 8.91276983, 3e-10)
            p1[0] shouldBe (-0.7632762548968159627 plusOrMinus 1e-12)
            p1[1] shouldBe (-0.6086337670823762701 plusOrMinus 1e-12)
            p1[2] shouldBe (-0.2167355431320546947 plusOrMinus 1e-12)
        }
        "eraApci" {
            val ebp = Vector3D(0.901310875, -0.417402664, -0.180982288)
            val ebv = Vector3D(0.00742727954, 0.0140507459, 0.00609045792)
            val ehp = Vector3D(0.903358544, -0.415395237, -0.180084014)
            val astrom = eraApci(2456165.5, 0.401182685, ebp, ebv, ehp, 0.0013122272, -2.92808623e-5, 3.05749468e-8)

            astrom.pmt shouldBe (12.65133794027378508 plusOrMinus 1e-11)
            astrom.eb[0] shouldBe (0.901310875 plusOrMinus 1e-12)
            astrom.eb[1] shouldBe (-0.417402664 plusOrMinus 1e-12)
            astrom.eb[2] shouldBe (-0.180982288 plusOrMinus 1e-12)
            astrom.eh[0] shouldBe (0.8940025429324143045 plusOrMinus 1e-12)
            astrom.eh[1] shouldBe (-0.4110930268679817955 plusOrMinus 1e-12)
            astrom.eh[2] shouldBe (-0.1782189004872870264 plusOrMinus 1e-12)
            astrom.em shouldBe (1.010465295811013146 plusOrMinus 1e-12)
            astrom.v[0] shouldBe (0.4289638913597693554e-4 plusOrMinus 1e-16)
            astrom.v[1] shouldBe (0.8115034051581320575e-4 plusOrMinus 1e-16)
            astrom.v[2] shouldBe (0.3517555136380563427e-4 plusOrMinus 1e-16)
            astrom.bm1 shouldBe (0.9999999951686012981 plusOrMinus 1e-12)
            astrom.bpn[0, 0] shouldBe (0.9999991390295159156 plusOrMinus 1e-12)
            astrom.bpn[1, 0] shouldBe (0.4978650072505016932e-7 plusOrMinus 1e-12)
            astrom.bpn[2, 0] shouldBe (0.1312227200000000000e-2 plusOrMinus 1e-12)
            astrom.bpn[0, 1] shouldBe (-0.1136336653771609630e-7 plusOrMinus 1e-12)
            astrom.bpn[1, 1] shouldBe (0.9999999995713154868 plusOrMinus 1e-12)
            astrom.bpn[2, 1] shouldBe (-0.2928086230000000000e-4 plusOrMinus 1e-12)
            astrom.bpn[0, 2] shouldBe (-0.1312227200895260194e-2 plusOrMinus 1e-12)
            astrom.bpn[1, 2] shouldBe (0.2928082217872315680e-4 plusOrMinus 1e-12)
            astrom.bpn[2, 2] shouldBe (0.9999991386008323373 plusOrMinus 1e-12)
        }
        "eraApcs13" {
            val p = Vector3D(-6241497.16, 401346.896, -1251136.04)
            val v = Vector3D(-29.264597, -455.021831, 0.0266151194)
            val astrom = eraApcs13(2456165.5, 0.401182685, p, v)

            astrom.pmt shouldBe (12.65133794027378508 plusOrMinus 1e-11)
            astrom.eb[0] shouldBe (0.9012691529025250644 plusOrMinus 1e-12)
            astrom.eb[1] shouldBe (-0.4173999812023194317 plusOrMinus 1e-12)
            astrom.eb[2] shouldBe (-0.1809906511146429670 plusOrMinus 1e-12)
            astrom.eh[0] shouldBe (0.8939939101760130792 plusOrMinus 1e-12)
            astrom.eh[1] shouldBe (-0.4111053891734021478 plusOrMinus 1e-12)
            astrom.eh[2] shouldBe (-0.1782336880636997374 plusOrMinus 1e-12)
            astrom.em shouldBe (1.010428384373491095 plusOrMinus 1e-12)
            astrom.v[0] shouldBe (0.4279877294121697570e-4 plusOrMinus 1e-16)
            astrom.v[1] shouldBe (0.7963255087052120678e-4 plusOrMinus 1e-16)
            astrom.v[2] shouldBe (0.3517564013384691531e-4 plusOrMinus 1e-16)
            astrom.bm1 shouldBe (0.9999999952947980978 plusOrMinus 1e-12)
            astrom.bpn shouldBe Matrix3D.IDENTITY
        }
        "eraAtioq" {
            val astrom =
                eraApio13(2456384.5, 0.969254051, 0.1550675, -0.527800806, -1.2345856, 2738.0, 2.47230737e-7, 1.82640464e-6, 731.0, 12.8, 0.59, 0.55)
            val (aob, zob, hob, dob, rob) = eraAtioq(2.710121572969038991, 0.1729371367218230438, astrom)

            aob shouldBe (0.9233952224895122499e-1 plusOrMinus 1e-12)
            zob shouldBe (1.407758704513549991 plusOrMinus 1e-12)
            hob shouldBe (-0.9247619879881698140e-1 plusOrMinus 1e-12)
            dob shouldBe (0.1717653435756234676 plusOrMinus 1e-12)
            rob shouldBe (2.710085107988480746 plusOrMinus 1e-12)
        }
        "eraApio13" {
            val astrom =
                eraApio13(2456384.5, 0.969254051, 0.1550675, -0.527800806, -1.2345856, 2738.0, 2.47230737e-7, 1.82640464e-6, 731.0, 12.8, 0.59, 0.55)

            astrom.along shouldBe (-0.5278008060295995733 plusOrMinus 1e-12)
            astrom.xpl shouldBe (0.1133427418130752958e-5 plusOrMinus 1e-17)
            astrom.ypl shouldBe (0.1453347595780646207e-5 plusOrMinus 1e-17)
            astrom.sphi shouldBe (-0.9440115679003211329 plusOrMinus 1e-12)
            astrom.cphi shouldBe (0.3299123514971474711 plusOrMinus 1e-12)
            astrom.diurab shouldBe (0.5135843661699913529e-6 plusOrMinus 1e-12)
            astrom.eral shouldBe (2.617608909189664000 plusOrMinus 1e-12)
            astrom.refa shouldBe (0.2014187785940396921e-3 plusOrMinus 1e-15)
            astrom.refb shouldBe (-0.2361408314943696227e-6 plusOrMinus 1e-18)
        }
        "eraApio" {
            val astrom =
                eraApio(-3.01974337e-11, 3.14540971, -0.527800806, -1.2345856, 2738.0, 2.47230737e-7, 1.82640464e-6, 0.000201418779, -2.36140831e-7)

            astrom.along shouldBe (-0.5278008060295995734 plusOrMinus 1e-12)
            astrom.xpl shouldBe (0.1133427418130752958e-5 plusOrMinus 1e-17)
            astrom.ypl shouldBe (0.1453347595780646207e-5 plusOrMinus 1e-17)
            astrom.sphi shouldBe (-0.9440115679003211329 plusOrMinus 1e-12)
            astrom.cphi shouldBe (0.3299123514971474711 plusOrMinus 1e-12)
            astrom.diurab shouldBe (0.5135843661699913529e-6 plusOrMinus 1e-12)
            astrom.eral shouldBe (2.617608903970400427 plusOrMinus 1e-12)
            astrom.refa shouldBe (0.2014187790000000000e-3 plusOrMinus 1e-15)
            astrom.refb shouldBe (-0.2361408310000000000e-6 plusOrMinus 1e-18)
        }
        "eraAtco13" {
            val (b, eo) = eraAtco13(
                2.71, 0.174, 1e-5, 5e-6, 0.1.arcsec, 55.0.kms, 2456384.5, 0.969254051,
                0.1550675, -0.527800806, -1.2345856, 2738.0, 2.47230737e-7, 1.82640464e-6, 731.0, 12.8,
                0.59, 0.55
            )

            val (aob, zob, hob, dob, rob) = b

            aob shouldBe (0.9251774485485515207e-1 plusOrMinus 1e-12)
            zob shouldBe (1.407661405256499357 plusOrMinus 1e-12)
            hob shouldBe (-0.9265154431529724692e-1 plusOrMinus 1e-12)
            dob shouldBe (0.1716626560072526200 plusOrMinus 1e-12)
            rob shouldBe (2.710260453504961012 plusOrMinus 1e-12)
            eo shouldBe (-0.003020548354802412839 plusOrMinus 1e-14)
        }
        "eraAticq" {
            val (astrom) = eraApci13(2456165.5, 0.401182685)
            val (ri, di) = eraAticq(2.710121572969038991, 0.1729371367218230438, astrom)
            ri shouldBe (2.710126504531716819 plusOrMinus 1e-12)
            di shouldBe (0.1740632537627034482 plusOrMinus 1e-12)
        }
        "eraAtic13" {
            val (rc, dc, eo) = eraAtic13(2.710121572969038991, 0.1729371367218230438, 2456165.5, 0.401182685)

            rc shouldBe (2.710126504531716819 plusOrMinus 1e-12)
            dc shouldBe (0.1740632537627034482 plusOrMinus 1e-12)
            eo shouldBe (-0.002900618712657375647 plusOrMinus 1e-14)
        }
        "eraS2pv" {
            val pv = eraS2pv(-3.21, 0.123, 0.456, -7.8e-6, 9.01e-6, -1.23e-5)

            pv.position[0] shouldBe (-0.4514964673880165228 plusOrMinus 1e-12)
            pv.position[1] shouldBe (0.0309339427734258688 plusOrMinus 1e-12)
            pv.position[2] shouldBe (0.0559466810510877933 plusOrMinus 1e-12)

            pv.velocity[0] shouldBe (0.1292270850663260170e-4 plusOrMinus 1e-16)
            pv.velocity[1] shouldBe (0.2652814182060691422e-5 plusOrMinus 1e-16)
            pv.velocity[2] shouldBe (0.2568431853930292259e-5 plusOrMinus 1e-16)
        }
        "eraStarpv" {
            val pv = eraStarpv(0.01686756, -1.093989828, -1.78323516e-5, 2.336024047e-6, 0.74723.arcsec, (-21.6).kms)

            pv.position[0] shouldBe (126668.5912743160601 plusOrMinus 1e-10)
            pv.position[1] shouldBe (2136.792716839935195 plusOrMinus 1e-12)
            pv.position[2] shouldBe (-245251.2339876830091 plusOrMinus 1e-10)

            pv.velocity[0] shouldBe (-0.4051854008955659551e-2 plusOrMinus 1e-13)
            pv.velocity[1] shouldBe (-0.6253919754414777970e-2 plusOrMinus 1e-15)
            pv.velocity[2] shouldBe (0.1189353714588109341e-1 plusOrMinus 1e-13)
        }
        "eraAtoiq" {
            val astrom =
                eraApio13(2456384.5, 0.969254051, 0.1550675, -0.527800806, -1.2345856, 2738.0, 2.47230737e-7, 1.82640464e-6, 731.0, 12.8, 0.59, 0.55)

            with(eraAtoiq('R', 2.710085107986886201, 0.1717653435758265198, astrom)) {
                this[0] shouldBe (2.710121574447540810 plusOrMinus 1e-12)
                this[1] shouldBe (0.17293718391166087785 plusOrMinus 1e-12)
            }
            with(eraAtoiq('H', -0.09247619879782006106, 0.1717653435758265198, astrom)) {
                this[0] shouldBe (2.710121574448138676 plusOrMinus 1e-12)
                this[1] shouldBe (0.1729371839116608778 plusOrMinus 1e-12)
            }
            with(eraAtoiq('A', 0.09233952224794989993, 1.407758704513722461, astrom)) {
                this[0] shouldBe (2.710121574448138676 plusOrMinus 1e-12)
                this[1] shouldBe (0.1729371839116608781 plusOrMinus 1e-12)
            }
        }
        "eraAtoc13" {
            // @formatter:off
            with(eraAtoc13('R', 2.710085107986886201, 0.1717653435758265198, 2456384.5, 0.969254051, 0.1550675, -0.527800806, -1.2345856, 2738.0, 2.47230737e-7, 1.82640464e-6, 731.0, 12.8, 0.59, 0.55)) {
                this[0] shouldBe (2.709956744659136129 plusOrMinus 1e-12)
                this[1] shouldBe (0.1741696500898471362 plusOrMinus 1e-12)
            }
            with(eraAtoc13('H', -0.09247619879782006106, 0.1717653435758265198, 2456384.5, 0.969254051, 0.1550675, -0.527800806, -1.2345856, 2738.0, 2.47230737e-7, 1.82640464e-6, 731.0, 12.8, 0.59, 0.55)) {
                this[0] shouldBe (2.709956744659734086 plusOrMinus 1e-12)
                this[1] shouldBe (0.1741696500898471362 plusOrMinus 1e-12)
            }
            with(eraAtoc13('A', 0.09233952224794989993, 1.407758704513722461, 2456384.5, 0.969254051, 0.1550675, -0.527800806, -1.2345856, 2738.0, 2.47230737e-7, 1.82640464e-6, 731.0, 12.8, 0.59, 0.55)) {
                this[0] shouldBe (2.709956744659734086 plusOrMinus 1e-12)
                this[1] shouldBe (0.1741696500898471366 plusOrMinus 1e-12)
            }
            // @formatter:on
        }
    }
}
