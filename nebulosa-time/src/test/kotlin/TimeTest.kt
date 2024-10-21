import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.test.download
import nebulosa.time.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.io.path.inputStream

class TimeTest {

    @Test
    fun convertJdToDatetime() {
        TimeJD(2459902.1234).asDateTime().toString() shouldBe "2022-11-18T14:57:41.759993433"
        TimeJD(2459902.6789).asDateTime().toString() shouldBe "2022-11-19T04:17:36.960014998"
        TimeJD(2299161.0).asDateTime().toString() shouldBe "1582-10-15T12:00"
        TimeJD(2299160.0).asDateTime(JulianCalendarCutOff.GREGORIAN_START).toString() shouldBe "1582-10-04T12:00"
    }

    @Test
    fun jdAsYearMonthDayAndFraction() {
        val (yearMonthDay, fraction) = TimeJD(2400000.5, 50123.9999).asYearMonthDayAndFraction()
        val (year, month, day) = yearMonthDay
        year shouldBeExactly 1996
        month shouldBeExactly 2
        day shouldBeExactly 10
        fraction[0] shouldBe (0.9999 plusOrMinus 1e-9)
    }

    @Test
    fun timeUnixAndTimeYmdhms() {
        val now = LocalDateTime.now(ZoneOffset.UTC).withNano(0)
        val unix = TimeUnix(now.toEpochSecond(ZoneOffset.UTC).toDouble()).value
        val ymdhms = TimeYMDHMS(now).value
        ymdhms shouldBeExactly unix
    }

    @Test
    fun highPrecisionJd() {
        val jd = TimeJD(2444495.5, 0.4788310185185185)
        jd.whole shouldBe (2444496.0 plusOrMinus 1E-16)
        jd.fraction shouldBe (-0.021168981481481497 plusOrMinus 1E-16)
    }

    @Test
    fun jd() {
        val jd = TimeJD(2444495.9788310183)
        jd.whole shouldBe (2444496.0 plusOrMinus 1E-9)
        jd.fraction shouldBe (-0.021168981 plusOrMinus 1E-9)
    }

    @Test
    fun ymdhms() {
        val jd = TimeYMDHMS(1980, 9, 12, 23, 29, 31.0)
        jd.whole shouldBe (2444495.0 plusOrMinus 1E-16)
        jd.fraction shouldBe (0.4788310185185185 plusOrMinus 1E-16)
    }

    @Test
    fun mjd() {
        val mdj = TimeMJD(50001.47883101852)
        mdj.whole shouldBe (2450002.0 plusOrMinus 1e-8)
        mdj.fraction shouldBe (-0.021168981482333038 plusOrMinus 1e-12)
    }

    @Test
    fun utc20220101120000() {
        val time = UTC(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0))

        with(time.utc) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-1.2782876157107722e-06 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0004282407407407707 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0008007407407407707 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0008121958147759566 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0008007396238607902 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.001055591574221432 plusOrMinus 1E-14)
        }
    }

    @Test
    fun utc20230601235959() {
        val time = UTC(TimeYMDHMS(2023, 6, 1, 23, 59, 59.0))

        with(time.utc) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49998842592592596 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.499987890906273 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49958333333333327 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49921083333333327 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49919901829547053 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49921082275980533 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49894796237489814 plusOrMinus 1E-14)
        }
    }

    @Test
    fun utc20240101000000() {
        val time = UTC(TimeYMDHMS(2024, 1, 1, 0, 0, 0.0))

        with(time.utc) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.5 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49999989822685187 plusOrMinus 1E-8)
        }
        with(time.tai) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49957175925925923 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49919925925925923 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49918729577550847 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.4991992606390423 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.4989330976467987 plusOrMinus 1E-14)
        }
    }

    @Test
    fun utc20251231175943() {
        val time = UTC(TimeYMDHMS(2025, 12, 31, 17, 59, 43.0))

        with(time.utc) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24980324074074078 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24980371005787044 plusOrMinus 1E-6)
        }
        with(time.tai) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.25023148148148155 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.25060398148148155 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2506164542459721 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2506039804506639 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.25088147386323706 plusOrMinus 1E-14)
        }
    }

    @Test
    fun ut120220101120000() {
        val time = UT1(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0))

        with(time.utc) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (1.2782876138691737e-06 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0004295190283546413 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0008020190283546413 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.000813474102390718 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0008020179114750974 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0010568698618555591 plusOrMinus 1E-14)
        }
    }

    @Test
    fun ut120230601235959() {
        val time = UT1(TimeYMDHMS(2023, 6, 1, 23, 59, 59.0))

        with(time.utc) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49998896094558004 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49998842592592596 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49958279831367913 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49921029831367913 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49919848327581595 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.4992102877401513 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.4989474273552358 plusOrMinus 1E-14)
        }
    }

    @Test
    fun ut120240101000000() {
        val time = UT1(TimeYMDHMS(2024, 1, 1, 0, 0, 0.0))

        with(time.utc) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4999998982268517 plusOrMinus 1E-8)
        }
        with(time.ut1) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.5 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.4995718610324076 plusOrMinus 1E-8)
        }
        with(time.tt) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.4991993610324076 plusOrMinus 1E-8)
        }
        with(time.tcg) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.4991873975486568 plusOrMinus 1E-8)
        }
        with(time.tdb) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49919936241219065 plusOrMinus 1E-8)
        }
        with(time.tcb) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49893319941994874 plusOrMinus 1E-8)
        }
    }

    @Test
    fun ut120251231175943() {
        val time = UT1(TimeYMDHMS(2025, 12, 31, 17, 59, 43.0))

        with(time.utc) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24980277142361115 plusOrMinus 1E-6)
        }
        with(time.ut1) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24980324074074078 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.25023101216435195 plusOrMinus 1E-6)
        }
        with(time.tt) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.25060351216435195 plusOrMinus 1E-6)
        }
        with(time.tcg) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2506159849288422 plusOrMinus 1E-6)
        }
        with(time.tdb) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.25060351113353413 plusOrMinus 1E-6)
        }
        with(time.tcb) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2508810045461 plusOrMinus 1E-6)
        }
    }

    @Test
    fun tai20220101120000() {
        val time = TAI(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0))

        with(time.utc) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.00042824074074071516 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.00042951902898347746 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.00037249999999999995 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.00038395507373673244 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.00037249888297375804 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0006273508266944423 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tai20230601235959() {
        val time = TAI(TimeYMDHMS(2023, 6, 1, 23, 59, 59.0))

        with(time.utc) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4995601851851852 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49955965016637977 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49998842592592596 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49963907407407404 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49962725903650973 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49963906350042875 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49937620312216147 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tai20240101000000() {
        val time = TAI(TimeYMDHMS(2024, 1, 1, 0, 0, 0.0))

        with(time.utc) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49957175925925923 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4995718610330666 plusOrMinus 1E-8)
        }
        with(time.tai) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.5 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.4996275 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49961553651654766 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.4996275013799246 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49936133839432106 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tai20251231175943() {
        val time = TAI(TimeYMDHMS(2025, 12, 31, 17, 59, 43.0))

        with(time.utc) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.249375 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24937546931712967 plusOrMinus 1E-6)
        }
        with(time.tai) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24980324074074078 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2501757407407408 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2501882135049329 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2501757397097831 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.25045323311571627 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tt20220101120000() {
        val time = TT(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0))

        with(time.utc) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.0008007407407407215 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.0008020190295288602 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.00037249999999999995 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (1.1455073477126416e-05 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-1.1171534654999348e-09 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0002548508207915326 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tt20230601235959() {
        val time = TT(TimeYMDHMS(2023, 6, 1, 23, 59, 59.0))

        with(time.utc) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4991876851851852 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.499187150167117 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49961592592592596 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49998842592592596 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.4999997590367693 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49998843649967323 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49974870312783515 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tt20240101000000() {
        val time = TT(TimeYMDHMS(2024, 1, 1, 0, 0, 0.0))

        with(time.utc) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49919925925925923 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49919936103364004 plusOrMinus 1E-8)
        }
        with(time.tai) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4996275 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.5 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49998803651680723 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4999999986199522 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.4997338384002199 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tt20251231175943() {
        val time = TT(TimeYMDHMS(2025, 12, 31, 17, 59, 43.0))

        with(time.utc) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24900250000000002 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24900296931712967 plusOrMinus 1E-6)
        }
        with(time.tai) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2494307407407408 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24980324074074078 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2498157135046733 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2498032397096612 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2500807331098187 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tcb20220101120000() {
        val time = TCB(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0))

        with(time.utc) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.001055591557493719 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.0010568698466550197 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.0006273508167529781 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.00025485081675297817 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.00024339574345346468 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.00025485193399348575 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tcb20230601235959() {
        val time = TCB(TimeYMDHMS(2023, 6, 1, 23, 59, 59.0))

        with(time.utc) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4989248142429501 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49892427922540217 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49935305498369087 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49972555498369087 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4997373700208124 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4997255655575102 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49998842592592596 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tcb20240101000000() {
        val time = TCB(TimeYMDHMS(2024, 1, 1, 0, 0, 0.0))

        with(time.utc) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.498933097663694 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4989331994384845 plusOrMinus 1E-8)
        }
        with(time.tai) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49936133840443475 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49973383840443475 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.499745801887442 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49973383702429897 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.5 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tcb20251231175943() {
        val time = TCB(TimeYMDHMS(2025, 12, 31, 17, 59, 43.0))

        with(time.utc) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24872500763531546 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24872547695244512 plusOrMinus 1E-6)
        }
        with(time.tai) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2491532483760562 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2495257483760562 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24953822113979535 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24952574734488586 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24980324074074078 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tcg20220101120000() {
        val time = TCG(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0))

        with(time.utc) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.0008121958142098429 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.000813474103014745 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.000383955073469143 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-1.1455073469143043e-05 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-1.1456190626520906e-05 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.00024339574714086394 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tcg20230601235959() {
        val time = TCG(TimeYMDHMS(2023, 6, 1, 23, 59, 59.0))

        with(time.utc) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49917587014788867 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49917533512984386 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49960411088862944 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49997661088862944 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49998842592592596 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49997662146238 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49976051816531164 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tcg20240101000000() {
        val time = TCG(TimeYMDHMS(2024, 1, 1, 0, 0, 0.0))

        with(time.utc) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4991872957760748 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49918739755047403 plusOrMinus 1E-8)
        }
        with(time.tai) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49961553651681556 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49998803651681556 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.5 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4999880351367638 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49974580188359374 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tcg20251231175943() {
        val time = TCG(TimeYMDHMS(2025, 12, 31, 17, 59, 43.0))

        with(time.utc) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24899002723607622 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24899049655320588 plusOrMinus 1E-6)
        }
        with(time.tai) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24941826797681696 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24979076797681696 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24980324074074078 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2497907669457333 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2500682603456974 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tdb20220101120000() {
        val time = TDB(TimeYMDHMS(2022, 1, 1, 12, 0, 0.0))

        with(time.utc) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.0008007396235872465 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.0008020179123753776 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.00037249888284653445 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (1.1171534654999348e-09 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (1.1456190630592697e-05 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2459581.0 plusOrMinus 1E-14)
            fraction shouldBe (0.0002548519379450154 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tdb20230601235959() {
        val time = TDB(TimeYMDHMS(2023, 6, 1, 23, 59, 59.0))

        with(time.utc) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.499187674611438 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4991871395933698 plusOrMinus 1E-14)
        }
        with(time.tai) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4996159153521787 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4999884153521787 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.4999997696105166 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2460097.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49998842592592596 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2460098.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.4997487137015826 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tdb20240101000000() {
        val time = TDB(TimeYMDHMS(2024, 1, 1, 0, 0, 0.0))

        with(time.utc) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.499199260639307 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.4991993624136878 plusOrMinus 1E-8)
        }
        with(time.tai) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.49962750138004774 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49999999861995226 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.4999880351367595 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2460310.0 plusOrMinus 1E-14)
            fraction shouldBe (0.5 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2460311.0 plusOrMinus 1E-14)
            fraction shouldBe (-0.49973383702017204 plusOrMinus 1E-14)
        }
    }

    @Test
    fun tdb20251231175943() {
        val time = TDB(TimeYMDHMS(2025, 12, 31, 17, 59, 43.0))

        with(time.utc) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24900250103107957 plusOrMinus 1E-14)
        }
        with(time.ut1) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24900297034820923 plusOrMinus 1E-6)
        }
        with(time.tai) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24943074177182037 plusOrMinus 1E-14)
        }
        with(time.tt) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24980324177182037 plusOrMinus 1E-14)
        }
        with(time.tcg) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24981571453575288 plusOrMinus 1E-14)
        }
        with(time.tdb) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.24980324074074078 plusOrMinus 1E-14)
        }
        with(time.tcb) {
            whole shouldBe (2461041.0 plusOrMinus 1E-14)
            fraction shouldBe (0.2500807341408983 plusOrMinus 1E-14)
        }
    }

    companion object {

        @JvmStatic
        @BeforeAll
        fun loadIERS() {
            val iersa = IERSA()
            val finals2000A = download("https://maia.usno.navy.mil/ser7/finals2000A.all")
            finals2000A.inputStream().use(iersa::load)

            val iersb = IERSB()
            val eopc04 = download("https://hpiers.obspm.fr/iers/eop/eopc04/eopc04.1962-now")
            eopc04.inputStream().use(iersb::load)

            IERS.attach(IERSAB(iersa, iersb))
        }
    }
}
