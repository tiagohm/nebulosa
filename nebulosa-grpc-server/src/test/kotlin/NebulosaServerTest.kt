import nebulosa.grpc.server.NebulosaServer
import nebulosa.test.NonGitHubOnly
import org.junit.jupiter.api.Test

@NonGitHubOnly
class NebulosaServerTest {

    @Test
    fun server() {
        NebulosaServer().use {
            it.run()
            Thread.sleep(600000)
        }
    }
}
