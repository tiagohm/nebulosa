import io.kotest.matchers.shouldBe
import nebulosa.io.Base64OutputStream
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.random.Random

class Base64OutputStreamTest {

    @Test
    fun noPadding() {
        val output = Base64OutputStream()
        output.write("Hello World!".encodeToByteArray())
        output.end()
        output.decoded() shouldBe "SGVsbG8gV29ybGQh"

        output.reset()
        output.write("Hello World!!!!".encodeToByteArray())
        output.end()
        output.decoded() shouldBe "SGVsbG8gV29ybGQhISEh"
    }

    @Test
    fun oneTrailingChar() {
        val output = Base64OutputStream()
        output.write("Hello World".encodeToByteArray())
        output.end()
        output.decoded() shouldBe "SGVsbG8gV29ybGQ="

        output.reset()

        output.write("Hello World!!!".encodeToByteArray())
        output.end()
        output.decoded() shouldBe "SGVsbG8gV29ybGQhISE="
    }

    @Test
    fun twoTrailingChar() {
        val output = Base64OutputStream()
        output.write("Hello World!!".encodeToByteArray())
        output.end()
        output.decoded() shouldBe "SGVsbG8gV29ybGQhIQ=="

        output.reset()

        output.write("Hello World!!!!!".encodeToByteArray())
        output.end()
        output.decoded() shouldBe "SGVsbG8gV29ybGQhISEhIQ=="
    }

    @Test
    fun ascii() {
        val output = Base64OutputStream()

        repeat(256) {
            output.write(it)
        }

        output.end()
        output.decoded() shouldBe "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4OTo7PD0+P0BBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3BxcnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouMjY6PkJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmqq6ytrq+wsbKztLW2t7i5uru8vb6/wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX2Nna29zd3t/g4eLj5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7/P3+/w=="
    }

    @Test
    fun random() {
        val output = Base64OutputStream()

        repeat(10000) {
            val text = Random.nextBytes(Random.nextInt(1, 1000))
            output.write(text)
            output.end()
            output.decoded() shouldBe Base64.getEncoder().encodeToString(text)
            output.reset()
        }
    }

    @Test
    fun noPaddingUrlSafe() {
        val output = Base64OutputStream(true)
        output.write("Hello World!".encodeToByteArray())
        output.end()
        output.decoded() shouldBe "SGVsbG8gV29ybGQh"

        output.reset()
        output.write("Hello World!!!!".encodeToByteArray())
        output.end()
        output.decoded() shouldBe "SGVsbG8gV29ybGQhISEh"
    }

    @Test
    fun oneTrailingCharUrlSafe() {
        val output = Base64OutputStream(true)
        output.write("Hello World".encodeToByteArray())
        output.end()
        output.decoded() shouldBe "SGVsbG8gV29ybGQ="

        output.reset()

        output.write("Hello World!!!".encodeToByteArray())
        output.end()
        output.decoded() shouldBe "SGVsbG8gV29ybGQhISE="
    }

    @Test
    fun twoTrailingCharUrlSafe() {
        val output = Base64OutputStream(true)
        output.write("Hello World!!".encodeToByteArray())
        output.end()
        output.decoded() shouldBe "SGVsbG8gV29ybGQhIQ=="

        output.reset()

        output.write("Hello World!!!!!".encodeToByteArray())
        output.end()
        output.decoded() shouldBe "SGVsbG8gV29ybGQhISEhIQ=="
    }

    @Test
    fun asciiUrlSafe() {
        val output = Base64OutputStream(true)

        repeat(256) {
            output.write(it)
        }

        output.end()
        output.decoded() shouldBe "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4OTo7PD0-P0BBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3BxcnN0dXZ3eHl6e3x9fn-AgYKDhIWGh4iJiouMjY6PkJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmqq6ytrq-wsbKztLW2t7i5uru8vb6_wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX2Nna29zd3t_g4eLj5OXm5-jp6uvs7e7v8PHy8_T19vf4-fr7_P3-_w=="
    }

    @Test
    fun randomUrlSafe() {
        val output = Base64OutputStream(true)

        repeat(10000) {
            val text = Random.nextBytes(Random.nextInt(1, 1000))
            output.write(text)
            output.end()
            output.decoded() shouldBe Base64.getUrlEncoder().encodeToString(text)
            output.reset()
        }
    }

    @Test
    fun endWithClose() {
        val output = Base64OutputStream()
        output.use { it.write("Hello World!!!!!".encodeToByteArray()) }
        output.decoded() shouldBe "SGVsbG8gV29ybGQhISEhIQ=="
    }
}
