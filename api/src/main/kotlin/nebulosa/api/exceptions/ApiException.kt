package nebulosa.api.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

open class ApiException(
    statusCode: HttpStatus,
    statusText: String = statusCode.reasonPhrase,
) : ResponseStatusException(statusCode, statusText)
