package nebulosa.api.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

sealed class ApiException(
    statusCode: HttpStatus,
    statusText: String = statusCode.reasonPhrase,
) : ResponseStatusException(statusCode, statusText)
