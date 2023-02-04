import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.constants.TAU
import nebulosa.erfa.*
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance.Companion.au
import nebulosa.math.Distance.Companion.km
import nebulosa.math.Distance.Companion.m
import nebulosa.math.Matrix3D
import nebulosa.math.Vector3D
import nebulosa.math.Velocity.Companion.auDay

@Suppress("FloatingPointLiteralPrecision")
class ErfaTest : StringSpec() {

    init {
        "eraC2s" {
            val (theta, phi) = eraC2s(100.0.au, (-50.0).au, 25.0.au)
            theta.value shouldBe (-0.4636476090008061162 plusOrMinus 1e-14)
            phi.value shouldBe (0.2199879773954594463 plusOrMinus 1e-14)
        }
        "eraP2s" {
            val (theta, phi, r) = eraP2s(100.0.au, (-50.0).au, 25.0.au)
            theta.value shouldBe (-0.4636476090008061162 plusOrMinus 1e-12)
            phi.value shouldBe (0.2199879773954594463 plusOrMinus 1e-12)
            r.value shouldBe (114.5643923738960002 plusOrMinus 1e-9)
        }
        "eraAnpm" {
            eraAnpm((-4.0).rad).value shouldBe (2.283185307179586477 plusOrMinus 1e-12)
        }
        "eraGc2Gde" {
            val (e1, p1, h1) = eraGc2Gde(6378137.0.m, 1.0 / 298.257223563, 2e6.m, 3e6.m, 5.244e6.m)
            e1.value shouldBe (0.9827937232473290680 plusOrMinus 1e-14)
            p1.value shouldBe (0.97160184819075459 plusOrMinus 1e-14)
            h1.meters shouldBe (331.4172461426059892 plusOrMinus 1e-8)

            val (e2, p2, h2) = eraGc2Gde(6378137.0.m, 1.0 / 298.257222101, 2e6.m, 3e6.m, 5.244e6.m)
            e2.value shouldBe (0.9827937232473290680 plusOrMinus 1e-14)
            p2.value shouldBe (0.97160184820607853 plusOrMinus 1e-14)
            h2.meters shouldBe (331.41731754844348 plusOrMinus 1e-8)

            val (e3, p3, h3) = eraGc2Gde(6378135.0.m, 1.0 / 298.26, 2e6.m, 3e6.m, 5.244e6.m)
            e3.value shouldBe (0.9827937232473290680 plusOrMinus 1e-14)
            p3.value shouldBe (0.9716018181101511937 plusOrMinus 1e-14)
            h3.meters shouldBe (333.2770726130318123 plusOrMinus 1e-8)
        }
        "eraGd2Gc" {
            val (x1, y1, z1) = eraGd2Gce(6378137.0.m, 1.0 / 298.257223563, 3.1.rad, (-0.5).rad, 2500.0.m)
            x1.meters shouldBe (-5599000.5577049947 plusOrMinus 1e-7)
            y1.meters shouldBe (233011.67223479203 plusOrMinus 1e-7)
            z1.meters shouldBe (-3040909.4706983363 plusOrMinus 1e-7)

            val (x2, y2, z2) = eraGd2Gce(6378137.0.m, 1.0 / 298.257222101, 3.1.rad, (-0.5).rad, 2500.0.m)
            x2.meters shouldBe (-5599000.5577260984 plusOrMinus 1e-7)
            y2.meters shouldBe (233011.6722356702949 plusOrMinus 1e-7)
            z2.meters shouldBe (-3040909.4706095476 plusOrMinus 1e-7)

            val (x3, y3, z3) = eraGd2Gce(6378135.0.m, 1.0 / 298.26, 3.1.rad, (-0.5).rad, 2500.0.m)
            x3.meters shouldBe (-5598998.7626301490 plusOrMinus 1e-7)
            y3.meters shouldBe (233011.5975297822211 plusOrMinus 1e-7)
            z3.meters shouldBe (-3040908.6861467111 plusOrMinus 1e-7)
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
                (-1836024.09).m, 1056607.72.m, (-5998795.26).m,
                -77.0361767, -133.310856, 0.0971855934,
                (-0.974170438).au, (-0.211520082).au, (-0.0917583024).au,
                0.00364365824.auDay, (-0.0154287319).auDay, (-0.00668922024).auDay,
                (-0.973458265).au, (-0.209215307).au, (-0.0906996477).au,
            )

            astro.pmt shouldBe (13.25248468622587269 plusOrMinus 1e-11)
            astro.eb.x.value shouldBe (-0.9741827110629881886 plusOrMinus 1e-12)
            astro.eb.y.value shouldBe (-0.2115130190136415986 plusOrMinus 1e-12)
            astro.eb.z.value shouldBe (-0.09179840186954412099 plusOrMinus 1e-12)
            astro.ehx shouldBe (-0.9736425571689454706 plusOrMinus 1e-12)
            astro.ehy shouldBe (-0.2092452125850435930 plusOrMinus 1e-12)
            astro.ehz shouldBe (-0.09075578152248299218 plusOrMinus 1e-12)
            astro.em.value shouldBe (0.9998233241709796859 plusOrMinus 1e-12)
            astro.vx shouldBe (0.2078704993282685510e-4 plusOrMinus 1e-16)
            astro.vy shouldBe (-0.8955360106989405683e-4 plusOrMinus 1e-16)
            astro.vz shouldBe (-0.3863338994289409097e-4 plusOrMinus 1e-16)
            astro.bm1 shouldBe (0.9999999950277561237 plusOrMinus 1e-12)
        }
        "eraApco" {
            val astro = eraApco(
                2456384.5, 0.970031644,
                (-0.974170438).au, (-0.211520082).au, (-0.0917583024).au,
                0.00364365824.auDay, (-0.0154287319).auDay, (-0.00668922024).auDay,
                (-0.973458265).au, (-0.209215307).au, (-0.0906996477).au,
                0.0013122272, -2.92808623e-5, 3.05749468e-8.rad,
                3.14540971.rad, (-0.527800806).rad, (-1.2345856).rad,
                2738.0.m,
                2.47230737e-7.rad, 1.82640464e-6.rad, (-3.01974337e-11).rad,
                0.000201418779.rad, (-2.36140831e-7).rad,
            )

            astro.pmt shouldBe (13.25248468622587269 plusOrMinus 1e-11)
            astro.eb.x.value shouldBe (-0.9741827110630322720 plusOrMinus 1e-12)
            astro.eb.y.value shouldBe (-0.2115130190135344832 plusOrMinus 1e-12)
            astro.eb.z.value shouldBe (-0.09179840186949532298 plusOrMinus 1e-12)
            astro.ehx shouldBe (-0.9736425571689739035 plusOrMinus 1e-12)
            astro.ehy shouldBe (-0.2092452125849330936 plusOrMinus 1e-12)
            astro.ehz shouldBe (-0.09075578152243272599 plusOrMinus 1e-12)
            astro.em.value shouldBe (0.9998233241709957653 plusOrMinus 1e-12)
            astro.vx shouldBe (0.2078704992916728762e-4 plusOrMinus 1e-16)
            astro.vy shouldBe (-0.8955360107151952319e-4 plusOrMinus 1e-16)
            astro.vz shouldBe (-0.3863338994288951082e-4 plusOrMinus 1e-16)
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
            astro.along.value shouldBe (-0.5278008060295995734 plusOrMinus 1e-12)
            astro.xpl.value shouldBe (0.1133427418130752958e-5 plusOrMinus 1e-17)
            astro.ypl.value shouldBe (0.1453347595780646207e-5 plusOrMinus 1e-17)
            astro.sphi shouldBe (-0.9440115679003211329 plusOrMinus 1e-12)
            astro.cphi shouldBe (0.3299123514971474711 plusOrMinus 1e-12)
            astro.eral.value shouldBe (2.617608903970400427 plusOrMinus 1e-12)
            astro.refa.value shouldBe (0.2014187790000000000e-3 plusOrMinus 1e-17)
            astro.refb.value shouldBe (-0.2361408310000000000e-6 plusOrMinus 1e-18)
            astro.diurab shouldBeExactly 0.0
        }
        "eraSp00" {
            eraSp00(2400000.5, 52541.0).value shouldBe (-0.6216698469981019309e-11 plusOrMinus 1e-12)
        }
        "eraObl06" {
            eraObl06(2400000.5, 54388.0).value shouldBe (0.4090749229387258204 plusOrMinus 1e-14)
        }
        "eraPfw06" {
            val (gamb, phib, psib, epsa) = eraPfw06(2400000.5, 50123.9999)
            gamb.value shouldBe (-0.2243387670997995690e-5 plusOrMinus 1e-16)
            phib.value shouldBe (0.4091014602391312808 plusOrMinus 1e-12)
            psib.value shouldBe (-0.9501954178013031895e-3 plusOrMinus 1e-14)
            epsa.value shouldBe (0.4091014316587367491 plusOrMinus 1e-12)
        }
        "eraFal03" {
            eraFal03(0.80).value shouldBe (5.132369751108684150 plusOrMinus 1e-12)
        }
        "eraFaf03" {
            eraFaf03(0.80).value shouldBe (0.2597711366745499518 plusOrMinus 1e-12)
        }
        "eraFaom03" {
            eraFaom03(0.80).value shouldBe (TAU - 5.973618440951302183 plusOrMinus 1e-12)
        }
        "eraFapa03" {
            eraFapa03(0.80).value shouldBe (0.1950884762240000000e-1 plusOrMinus 1e-12)
        }
        "eraFame03" {
            eraFame03(0.80).value shouldBe (5.417338184297289661 plusOrMinus 1e-12)
        }
        "eraFave03" {
            eraFave03(0.80).value shouldBe (3.424900460533758000 plusOrMinus 1e-12)
        }
        "eraFae03" {
            eraFae03(0.80).value shouldBe (1.744713738913081846 plusOrMinus 1e-12)
        }
        "eraFama03" {
            eraFama03(0.80).value shouldBe (3.275506840277781492 plusOrMinus 1e-12)
        }
        "eraFaju03" {
            eraFaju03(0.80).value shouldBe (5.275711665202481138 plusOrMinus 1e-12)
        }
        "eraFasa03" {
            eraFasa03(0.80).value shouldBe (5.371574539440827046 plusOrMinus 1e-12)
        }
        "eraFaur03" {
            eraFaur03(0.80).value shouldBe (5.180636450180413523 plusOrMinus 1e-12)
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
            era00(2400000.0 + 54388.0, 0.5).value shouldBe (0.4022837240028158102 plusOrMinus 1e-12)
        }
        "eraRefco" {
            val (refa, refb) = eraRefco(800.0, 10.0, 0.9, 0.4)
            refa.value shouldBe (0.2264949956241415009e-3 plusOrMinus 1e-15)
            refb.value shouldBe (-0.2598658261729343970e-6 plusOrMinus 1e-18)
        }
        "eraApcg" {
            val astrom = eraApcg(
                2456165.5, 0.401182685,
                0.901310875.au, (-0.417402664).au, (-0.180982288).au,
                0.00742727954.auDay, 0.0140507459.auDay, 0.00609045792.auDay,
                0.903358544.au, (-0.415395237).au, (-0.180084014).au
            )

            astrom.pmt shouldBe (12.65133794027378508 plusOrMinus 1e-11)
            astrom.eb.x.value shouldBe (0.901310875 plusOrMinus 1e-12)
            astrom.eb.y.value shouldBe (-0.417402664 plusOrMinus 1e-12)
            astrom.eb.z.value shouldBe (-0.180982288 plusOrMinus 1e-12)
            astrom.ehx shouldBe (0.8940025429324143045 plusOrMinus 1e-12)
            astrom.ehy shouldBe (-0.4110930268679817955 plusOrMinus 1e-12)
            astrom.ehz shouldBe (-0.1782189004872870264 plusOrMinus 1e-12)
            astrom.em.value shouldBe (1.010465295811013146 plusOrMinus 1e-12)
            astrom.vx shouldBe (0.4289638913597693554e-4 plusOrMinus 1e-16)
            astrom.vy shouldBe (0.8115034051581320575e-4 plusOrMinus 1e-16)
            astrom.vz shouldBe (0.3517555136380563427e-4 plusOrMinus 1e-16)
            astrom.bm1 shouldBe (0.9999999951686012981 plusOrMinus 1e-12)
        }
        "eraEpv00" {
            val (a, b, c, d) = eraEpv00(2400000.5, 53411.52501161)
            a[0] shouldBe (-0.7757238809297706813 plusOrMinus 1e-14)
            a[1] shouldBe (0.5598052241363340596 plusOrMinus 1e-14)
            a[2] shouldBe (0.2426998466481686993 plusOrMinus 1e-14)

            b[0] shouldBe (-0.1091891824147313846e-1 plusOrMinus 1e-15)
            b[1] shouldBe (-0.1247187268440845008e-1 plusOrMinus 1e-15)
            b[2] shouldBe (-0.5407569418065039061e-2 plusOrMinus 1e-15)

            c[0] shouldBe (-0.7714104440491111971 plusOrMinus 1e-14)
            c[1] shouldBe (0.5598412061824171323 plusOrMinus 1e-14)
            c[2] shouldBe (0.2425996277722452400 plusOrMinus 1e-14)

            d[0] shouldBe (-0.1091874268116823295e-1 plusOrMinus 1e-15)
            d[1] shouldBe (-0.1246525461732861538e-1 plusOrMinus 1e-15)
            d[2] shouldBe (-0.5404773180966231279e-2 plusOrMinus 1e-15)
        }
        "eraApcg13" {
            val astrom = eraApcg13(2456165.5, 0.401182685)

            astrom.pmt shouldBe (12.65133794027378508 plusOrMinus 1e-11)
            astrom.eb.x.value shouldBe (0.9013108747340644755 plusOrMinus 1e-12)
            astrom.eb.y.value shouldBe (-0.4174026640406119957 plusOrMinus 1e-12)
            astrom.eb.z.value shouldBe (-0.1809822877867817771 plusOrMinus 1e-12)
            astrom.ehx shouldBe (0.8940025429255499549 plusOrMinus 1e-12)
            astrom.ehy shouldBe (-0.4110930268331896318 plusOrMinus 1e-12)
            astrom.ehz shouldBe (-0.1782189006019749850 plusOrMinus 1e-12)
            astrom.em.value shouldBe (1.010465295964664178 plusOrMinus 1e-12)
            astrom.vx shouldBe (0.4289638912941341125e-4 plusOrMinus 1e-16)
            astrom.vy shouldBe (0.8115034032405042132e-4 plusOrMinus 1e-16)
            astrom.vz shouldBe (0.3517555135536470279e-4 plusOrMinus 1e-16)
            astrom.bm1 shouldBe (0.9999999951686013142 plusOrMinus 1e-12)
        }
        "eraAe2hd" {
            val (ha, dec) = eraAe2hd(5.5.rad, 1.1.rad, 0.7.rad)

            ha.value shouldBe (0.5933291115507309663 plusOrMinus 1E-14)
            dec.value shouldBe (0.9613934761647817620 plusOrMinus 1E-14)
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
            val pv = eraPvtob(2.0.rad, 0.5.rad, 3000.0.m, 1e-6.rad, (-0.5e-6).rad, 1e-8.rad, 5.0.rad)
            pv.first[0] shouldBe (4225081.367071159207 plusOrMinus 1e-5)
            pv.first[1] shouldBe (3681943.215856198144 plusOrMinus 1e-5)
            pv.first[2] shouldBe (3041149.399241260785 plusOrMinus 1e-5)
            pv.second[0] shouldBe (-268.4915389365998787 plusOrMinus 1e-5)
            pv.second[1] shouldBe (308.0977983288903123 plusOrMinus 1e-5)
            pv.second[2] shouldBe (0.0 plusOrMinus 1e-5)
        }
        "eraNut00a" {
            val (psi, eps) = eraNut00a(2400000.5, 53736.0)
            psi.value shouldBe (-0.9630909107115518431e-5 plusOrMinus 1e-13)
            eps.value shouldBe (0.4063239174001678710e-4 plusOrMinus 1e-13)
        }
        "eraNut06a" {
            val (psi, eps) = eraNut06a(2400000.5, 53736.0)
            psi.value shouldBe (-0.9630912025820308797e-5 plusOrMinus 1e-13)
            eps.value shouldBe (0.4063238496887249798e-4 plusOrMinus 1e-13)
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
            eect.value shouldBe (0.2046085004885125264e-8 plusOrMinus 1e-20)
        }
        "eraEra00" {
            val era00 = eraEra00(2454388.0, 0.5)
            era00.value shouldBe (0.4022837240028158102 plusOrMinus 1e-12)
        }
        "eraEe00" {
            val ee = eraEe00(2453736.0, 0.5, 0.4090789763356509900.rad, (-0.9630909107115582393e-5).rad)
            ee.value shouldBe (-0.8834193235367965479e-5 plusOrMinus 1e-18)
        }
        "eraGmst00" {
            val theta = eraGmst00(2453736.0, 0.5, 2453736.0, 0.5)
            theta.value shouldBe (1.754174972210740592 plusOrMinus 1e-12)
        }
        "eraGmst06" {
            val theta = eraGmst06(2453736.0, 0.5, 2453736.0, 0.5)
            theta.value shouldBe (1.754174971870091203 plusOrMinus 1e-12)
        }
        "eraEe06a" {
            val ee = eraEe06a(2453736.0, 0.5)
            ee.value shouldBe (-0.8834195072043790156e-5 plusOrMinus 1e-15)
        }
        "eraGst06a" {
            val theta = eraGst06a(2453736.0, 0.5, 2453736.0, 0.5)
            theta.value shouldBe (1.754166137675019159 plusOrMinus 1e-12)
        }
        "eraGst06" {
            val rnpb = Matrix3D(
                0.9999989440476103608, -0.1332881761240011518e-2, -0.5790767434730085097e-3,
                0.1332858254308954453e-2, 0.9999991109044505944, -0.4097782710401555759e-4,
                0.5791308472168153320e-3, 0.4020595661593994396e-4, 0.9999998314954572365,
            )

            val theta = eraGst06(2453736.0, 0.5, 2453736.0, 0.5, rnpb)
            theta.value shouldBe (1.754166138018167568 plusOrMinus 1e-12)
        }
        "eraS06" {
            val s = eraS06(2400000.5, 53736.0, 0.5791308486706011000e-3, 0.4020579816732961219e-4)
            s.value shouldBe (-0.1220032213076463117e-7 plusOrMinus 1e-18)
        }
        "eraS06a" {
            val s = eraS06a(2400000.5, 52541.0)
            s.value shouldBe (-0.1340680437291812383e-7 plusOrMinus 1e-18)
        }
        "eraEors" {
            val rnpb = Matrix3D(
                0.9999989440476103608, -0.1332881761240011518e-2, -0.5790767434730085097e-3,
                0.1332858254308954453e-2, 0.9999991109044505944, -0.4097782710401555759e-4,
                0.5791308472168153320e-3, 0.4020595661593994396e-4, 0.9999998314954572365,
            )

            val eo = eraEors(rnpb, (-0.1220040848472271978e-7).rad)
            eo.value shouldBe (-0.1332882715130744606e-2 plusOrMinus 1e-14)
        }
        "eraGst00a" {
            val theta = eraGst00a(2453736.0, 0.5, 2453736.0, 0.5)
            theta.value shouldBe (1.754166138018281369 plusOrMinus 1e-12)
        }
        "eraGst00b" {
            val theta = eraGst00b(2453736.0, 0.5)
            theta.value shouldBe (1.754166136510680589 plusOrMinus 1e-12)
        }
        "eraEe00a" {
            val ee = eraEe00a(2453736.0, 0.5)
            ee.value shouldBe (-0.8834192459222588227e-5 plusOrMinus 1e-18)
        }
        "eraEe00b" {
            val ee = eraEe00b(2453736.0, 0.5)
            ee.value shouldBe (-0.8835700060003032831e-5 plusOrMinus 1e-18)
        }
        "eraPr00" {
            val (dpsipr, depspr) = eraPr00(2453736.0, 0.5)
            dpsipr.value shouldBe (-0.8716465172668347629e-7 plusOrMinus 1e-22)
            depspr.value shouldBe (-0.7342018386722813087e-8 plusOrMinus 1e-22)
        }
        "eraObl80" {
            val eps0 = eraObl80(2454388.0, 0.5)
            eps0.value shouldBe (0.4090751347643816218 plusOrMinus 1e-14)
        }
        "eraNut00b" {
            val (dpsi, deps) = eraNut00b(2453736.0, 0.5)
            dpsi.value shouldBe (-0.9632552291148362783e-5 plusOrMinus 1e-13)
            deps.value shouldBe (0.4063197106621159367e-4 plusOrMinus 1e-13)
        }
        "eraC2tcio" {
            val rc2i = Matrix3D(
                0.9999998323037164738, 0.5581526271714303683e-9, 0.5791308477073443903e-3,
                0.2384266227524722273e-7, 0.9999999991917404296, 0.4020594955030704125e-4,
                0.5791308472168153320e-3, 0.4020595661593994396e-4, 0.9999998314954572365,
            )

            val rpom = Matrix3D(
                0.9999999999999674705, -0.1367174580728847031e-10, 0.2550602379999972723e-6,
                0.1414624947957029721e-10, 0.9999999999982694954, -0.1860359246998866338e-5,
                -0.2550602379741215275e-6, 0.1860359247002413923e-5, 0.9999999999982369658,
            )

            val rc2t = eraC2tcio(rc2i, 1.75283325530307.rad, rpom)

            rc2t[0, 0] shouldBe (-0.1810332128307110439 plusOrMinus 1e-12)
            rc2t[0, 1] shouldBe (0.9834769806938470149 plusOrMinus 1e-12)
            rc2t[0, 2] shouldBe (0.6555535638685466874e-4 plusOrMinus 1e-12)
            rc2t[1, 0] shouldBe (-0.9834768134135996657 plusOrMinus 1e-12)
            rc2t[1, 1] shouldBe (-0.1810332203649448367 plusOrMinus 1e-12)
            rc2t[1, 2] shouldBe (0.5749801116141106528e-3 plusOrMinus 1e-12)
            rc2t[2, 0] shouldBe (0.5773474014081407076e-3 plusOrMinus 1e-12)
            rc2t[2, 1] shouldBe (0.3961832391772658944e-4 plusOrMinus 1e-12)
            rc2t[2, 2] shouldBe (0.9999998325501691969 plusOrMinus 1e-12)
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

            e.eps0.value shouldBe (0.4090926006005828715 plusOrMinus 1e-14)
            e.psia.value shouldBe (0.6664369630191613431e-3 plusOrMinus 1e-14)
            e.oma.value shouldBe (0.4090925973783255982 plusOrMinus 1e-14)
            e.bpa.value shouldBe (0.5561149371265209445e-6 plusOrMinus 1e-14)
            e.bqa.value shouldBe (-0.6191517193290621270e-5 plusOrMinus 1e-14)
            e.pia.value shouldBe (0.6216441751884382923e-5 plusOrMinus 1e-14)
            e.bpia.value shouldBe (3.052014180023779882 plusOrMinus 1e-14)
            e.epsa.value shouldBe (0.4090864054922431688 plusOrMinus 1e-14)
            e.chia.value shouldBe (0.1387703379530915364e-5 plusOrMinus 1e-14)
            e.za.value shouldBe (0.2921789846651790546e-3 plusOrMinus 1e-14)
            e.zetaa.value shouldBe (0.3178773290332009310e-3 plusOrMinus 1e-14)
            e.thetaa.value shouldBe (0.2650932701657497181e-3 plusOrMinus 1e-14)
            e.pa.value shouldBe (0.6651637681381016288e-3 plusOrMinus 1e-14)
            e.gam.value shouldBe (0.1398077115963754987e-5 plusOrMinus 1e-14)
            e.phi.value shouldBe (0.4090864090837462602 plusOrMinus 1e-14)
            e.psi.value shouldBe (0.6664464807480920325e-3 plusOrMinus 1e-14)
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
    }
}
