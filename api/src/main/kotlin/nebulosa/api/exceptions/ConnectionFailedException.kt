package nebulosa.api.exceptions

import org.springframework.http.HttpStatus

object ConnectionFailedException : ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Connection Failed")
