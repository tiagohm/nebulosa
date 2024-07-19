import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldEndWith
import nebulosa.test.LinuxOnly
import org.junit.jupiter.api.Test

@LinuxOnly
class SystemPropertyTest {

    @Test
    fun rootDir() {
        System.getProperty("root.dir").shouldNotBeNull() shouldEndWith ("/nebulosa")
    }

    @Test
    fun projectDir() {
        System.getProperty("project.dir").shouldNotBeNull() shouldEndWith ("/nebulosa-test")
    }

    @Test
    fun github() {
        System.getProperty("github").shouldNotBeNull()
    }
}
