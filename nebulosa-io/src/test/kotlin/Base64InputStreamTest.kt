import io.kotest.matchers.shouldBe
import nebulosa.io.Base64InputStream
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.random.Random

class Base64InputStreamTest {

    @Test
    fun random() {
        repeat(1000) {
            val bytes = Random.nextBytes(Random.nextInt(1, 1000))
            val encoded = Base64.getEncoder().encodeToString(bytes)
            val stream = Base64InputStream(encoded)
            stream.readAllBytes() shouldBe bytes
        }
    }

    @Test
    fun randomUrlSafe() {
        repeat(1000) {
            val bytes = Random.nextBytes(Random.nextInt(1, 1000))
            val encoded = Base64.getUrlEncoder().encodeToString(bytes)
            val stream = Base64InputStream(encoded)
            stream.readAllBytes() shouldBe bytes
        }
    }
}
