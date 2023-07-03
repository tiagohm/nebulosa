package nebulosa.api.exceptions

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpStatusCodeException

open class ApiException(
    statusCode: HttpStatus, statusText: String = statusCode.reasonPhrase,
    httpHeaders: HttpHeaders = HttpHeaders.EMPTY,
) : HttpStatusCodeException(statusCode, statusText, httpHeaders, null, null)
