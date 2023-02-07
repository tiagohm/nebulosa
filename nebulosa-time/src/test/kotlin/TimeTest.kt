import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.io.resource
import nebulosa.time.*
import java.time.LocalDateTime
import java.time.ZoneOffset

class TimeTest : StringSpec() {

    init {
        IERSA.load(resource("finals2000A.all")!!)
        IERS.current = IERSA

        "convert jd to datetime" {
            TimeJD(2459902.1234).asDateTime().toString() shouldBe "2022-11-18T14:57:41.759993433"
            TimeJD(2459902.6789).asDateTime().toString() shouldBe "2022-11-19T04:17:36.960014998"
            TimeJD(2299161.0).asDateTime().toString() shouldBe "1582-10-15T12:00"
            TimeJD(2299160.0).asDateTime(JulianCalendarCutOff.GREGORIAN_START).toString() shouldBe "1582-10-04T12:00"
        }
        "jd as year, month, day and fraction" {
            val (year, month, day, fraction) = TimeJD(2400000.5, 50123.9999).asYearMonthDayAndFraction()
            year.toInt() shouldBeExactly 1996
            month.toInt() shouldBeExactly 2
            day.toInt() shouldBeExactly 10
            fraction shouldBe (0.9999 plusOrMinus 1e-9)
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
        "ut1 -> utc" {
            val time = UT1(TimeYMDHMS(2023, 1, 1, 12, 24, 48.123)).utc
            time.whole shouldBe (2459946.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.017223876663362683 plusOrMinus 1E-15)
        }
        "utc -> tai" {
            val time = UTC(TimeYMDHMS(2023, 1, 1, 12, 24, 48.123)).tai
            time.whole shouldBe (2459946.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.017651886574074127 plusOrMinus 1E-16)
        }
        "tai -> tt" {
            val time = TAI(TimeYMDHMS(2023, 1, 1, 12, 24, 48.123)).tt
            time.whole shouldBe (2459946.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.017596145833333358 plusOrMinus 1E-16)
        }
        "tt -> tcg" {
            val time = TT(TimeYMDHMS(2023, 1, 1, 12, 24, 48.123)).tcg
            time.whole shouldBe (2459946.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.01723535529790421 plusOrMinus 1E-16)
        }
        "tt -> tdb" {
            val time = TT(TimeYMDHMS(2023, 1, 1, 12, 24, 48.123)).tdb
            time.whole shouldBe (2459946.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.017223644633339433 plusOrMinus 1E-16)
        }
        "tdb -> tcb" {
            val time = TDB(TimeYMDHMS(2023, 1, 1, 12, 24, 48.123)).tcb
            time.whole shouldBe (2459946.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.01748415743557536 plusOrMinus 1E-16)
        }
        "tcb -> tdb" {
            val time = TCB(TimeYMDHMS(2023, 1, 1, 12, 24, 48.123)).tdb
            time.whole shouldBe (2459946.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.01696313423513064 plusOrMinus 1E-16)
        }
        "tdb -> tt" {
            val time = TDB(TimeYMDHMS(2023, 1, 1, 12, 24, 48.123)).tt
            time.whole shouldBe (2459946.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.01722364703332728 plusOrMinus 1E-16)
        }
        "tcg -> tt" {
            val time = TCG(TimeYMDHMS(2023, 1, 1, 12, 24, 48.123)).tt
            time.whole shouldBe (2459946.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.017211936368770664 plusOrMinus 1E-16)
        }
        "tt -> tai" {
            val time = TT(TimeYMDHMS(2023, 1, 1, 12, 24, 48.123)).tai
            time.whole shouldBe (2459946.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.016851145833333355 plusOrMinus 1E-16)
        }
        "tai -> utc" {
            val time = TAI(TimeYMDHMS(2023, 1, 1, 12, 24, 48.123)).utc
            time.whole shouldBe (2459946.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.016795405092592586 plusOrMinus 1E-16)
        }
        "utc -> ut1" {
            val time = UTC(TimeYMDHMS(2023, 1, 1, 12, 24, 48.123)).ut1
            time.whole shouldBe (2459946.0 plusOrMinus 1E-16)
            time.fraction shouldBe (0.017223415003304474 plusOrMinus 1E-16)
        }
    }
}
