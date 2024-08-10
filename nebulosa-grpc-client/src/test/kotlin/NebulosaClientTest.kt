import nebulosa.grpc.client.NebulosaClient
import nebulosa.test.NonGitHubOnly
import org.junit.jupiter.api.Test

@NonGitHubOnly
class NebulosaClientTest {

    @Test
    fun client() {
        val client = NebulosaClient()
        client.bark()
    }
}
