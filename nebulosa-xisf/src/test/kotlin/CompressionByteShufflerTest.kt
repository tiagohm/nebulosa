import io.kotest.matchers.shouldBe
import nebulosa.xisf.CompressionByteShuffler
import org.junit.jupiter.api.Test
import kotlin.random.Random

class CompressionByteShufflerTest {

    @Test
    fun shuffleAndUnshuffle() {
        val original = ByteArray(256) { (it / 32).toByte() }
        val buffer = ByteArray(256)
        val unshuffled = ByteArray(256)

        CompressionByteShuffler.shuffle(original, buffer, 8)
        CompressionByteShuffler.unshuffle(buffer, unshuffled, 8)

        unshuffled shouldBe original
    }

    @Test
    fun randomShuffleAndUnshuffle() {
        val original = Random.nextBytes(256)
        val buffer = ByteArray(256)
        val unshuffled = ByteArray(256)

        CompressionByteShuffler.shuffle(original, buffer, 8)
        CompressionByteShuffler.unshuffle(buffer, unshuffled, 8)

        unshuffled shouldBe original
    }
}
