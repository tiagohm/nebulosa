package nebulosa.api.internal

import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseExtractor
import java.nio.file.Path
import kotlin.io.path.outputStream

internal class InputStreamIntoPathExtractor(val path: Path) : ResponseExtractor<Long> {

    override fun extractData(response: ClientHttpResponse): Long {
        return path.outputStream().use { response.body.transferTo(it) }
    }
}
