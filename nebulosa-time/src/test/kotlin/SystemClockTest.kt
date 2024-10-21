import io.kotest.matchers.longs.shouldBeLessThanOrEqual
import io.kotest.matchers.types.shouldBeSameInstanceAs
import nebulosa.time.SystemClock
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.TimeZone

class SystemClockTest {

    @Test
    fun systemDefault() {
        ChronoUnit.MICROS.between(LocalDateTime.now(SystemClock), LocalDateTime.now()) shouldBeLessThanOrEqual 20000L
        SystemClock.zone shouldBeSameInstanceAs ZoneId.systemDefault()

        TimeZone.setDefault(TimeZone.getTimeZone("America/Manaus"))

        ChronoUnit.MICROS.between(LocalDateTime.now(SystemClock), LocalDateTime.now()) shouldBeLessThanOrEqual 20000L
        SystemClock.zone shouldBeSameInstanceAs ZoneId.systemDefault()
    }
}
