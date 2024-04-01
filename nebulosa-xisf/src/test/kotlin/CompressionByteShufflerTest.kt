import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.xisf.CompressionByteShuffler
import kotlin.random.Random

class CompressionByteShufflerTest : StringSpec() {

    init {
        "shuffle & unshuffle" {
            val original = ByteArray(256) { (it / 32).toByte() }
            val buffer = ByteArray(256)
            val unshuffled = ByteArray(256)

            CompressionByteShuffler.shuffle(original, buffer, 8)
            CompressionByteShuffler.unshuffle(buffer, unshuffled, 8)

            repeat(original.size) { unshuffled[it].toInt() shouldBeExactly original[it].toInt() }
        }
        "random shuffle & unshuffle" {
            val original = Random.nextBytes(256)
            val buffer = ByteArray(256)
            val unshuffled = ByteArray(256)

            CompressionByteShuffler.shuffle(original, buffer, 8)
            CompressionByteShuffler.unshuffle(buffer, unshuffled, 8)

            repeat(original.size) { unshuffled[it].toInt() shouldBeExactly original[it].toInt() }
        }
    }
}
