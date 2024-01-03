import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.time.*
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.io.path.inputStream

class TimeTest : StringSpec() {

    init {
        val iersa = IERSA()
        iersa.load(Path.of("../data/finals2000A.all").inputStream())
        IERS.attach(iersa)

        "convert jd to datetime" {
            TimeJD(2459902.1234).asDateTime().toString() shouldBe "2022-11-18T14:57:41.759993433"
            TimeJD(2459902.6789).asDateTime().toString() shouldBe "2022-11-19T04:17:36.960014998"
            TimeJD(2299161.0).asDateTime().toString() shouldBe "1582-10-15T12:00"
            TimeJD(2299160.0).asDateTime(JulianCalendarCutOff.GREGORIAN_START).toString() shouldBe "1582-10-04T12:00"
        }
        "jd as year, month, day and fraction" {
            val (yearMonthDay, fraction) = TimeJD(2400000.5, 50123.9999).asYearMonthDayAndFraction()
            val (year, month, day) = yearMonthDay
            year shouldBeExactly 1996
            month shouldBeExactly 2
            day shouldBeExactly 10
            fraction[0] shouldBe (0.9999 plusOrMinus 1e-9)
        }
        "time unix & time ymdhms" {
            val now = LocalDateTime.now(ZoneOffset.UTC).withNano(0)
            val unix = TimeUnix(now.toEpochSecond(ZoneOffset.UTC).toDouble()).value
            val ymdhms = TimeYMDHMS(now).value
            ymdhms shouldBeExactly unix
        }
        "high precision jd" {
            val jd = TimeJD(2444495.5, 0.4788310185185185)
            jd.whole shouldBe (2444496.0 plusOrMinus 1E-16)
            jd.fraction shouldBe (-0.021168981481481497 plusOrMinus 1E-16)
        }
        "jd" {
            val jd = TimeJD(2444495.9788310183)
            jd.whole shouldBe (2444496.0 plusOrMinus 1E-9)
            jd.fraction shouldBe (-0.021168981 plusOrMinus 1E-9)
        }
        "ymdhms" {
            val jd = TimeYMDHMS(1980, 9, 12, 23, 29, 31.0)
            jd.whole shouldBe (2444495.0 plusOrMinus 1E-16)
            jd.fraction shouldBe (0.4788310185185185 plusOrMinus 1E-16)
        }
        "mjd" {
            val mdj = TimeMJD(50001.47883101852)
            mdj.whole shouldBe (2450002.0 plusOrMinus 1e-8)
            mdj.fraction shouldBe (-0.021168981482333038 plusOrMinus 1e-12)
        }
        "ut1" {
            val time = UT1(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0))

            time.whole shouldBe (2459581.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.0 plusOrMinus 1E-15)

            with(time.utc) {
                whole shouldBe (2459581.0 plusOrMinus 1E-16)
                fraction shouldBe (1.2782876138691737e-06 plusOrMinus 1E-10)
            }
        }
        "utc" {
            val time = UTC(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0))

            time.whole shouldBe (2459581.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.0 plusOrMinus 1E-15)

            with(time.ut1) {
                whole shouldBe (2459581.0 plusOrMinus 1E-16)
                fraction shouldBe (-1.2782876157107722e-06 plusOrMinus 1E-10)
            }
        }
        "utc to tai" {
            val time = UTC(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).tai
            time.whole shouldBe (2459581.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.017651886574074127 plusOrMinus 1E-16)
        }
        "tai to tt" {
            val time = TAI(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).tt
            time.whole shouldBe (2459581.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.017596145833333358 plusOrMinus 1E-16)
        }
        "tt to tcg" {
            val time = TT(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).tcg
            time.whole shouldBe (2459581.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.01723535529790421 plusOrMinus 1E-16)
        }
        "tt to tdb" {
            val time = TT(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).tdb
            time.whole shouldBe (2459581.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.017223644633339433 plusOrMinus 1E-16)
        }
        "tdb to tcb" {
            val time = TDB(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).tcb
            time.whole shouldBe (2459581.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.01748415743557536 plusOrMinus 1E-16)
        }
        "tcb to tdb" {
            val time = TCB(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).tdb
            time.whole shouldBe (2459581.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.01696313423513064 plusOrMinus 1E-16)
        }
        "tdb to tt" {
            val time = TDB(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).tt
            time.whole shouldBe (2459581.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.01722364703332728 plusOrMinus 1E-16)
        }
        "tcg to tt" {
            val time = TCG(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).tt
            time.whole shouldBe (2459581.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.017211936368770664 plusOrMinus 1E-16)
        }
        "tt to tai" {
            val time = TT(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).tai
            time.whole shouldBe (2459581.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.016851145833333355 plusOrMinus 1E-16)
        }
        "tai to utc" {
            val time = TAI(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0)).utc
            time.whole shouldBe (2459581.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.016795405092592586 plusOrMinus 1E-16)
        }
    }
}
