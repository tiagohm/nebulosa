package nebulosa.api.exceptions

import org.springframework.http.HttpStatus

object DeviceNotFound : ApiException(HttpStatus.NOT_FOUND, "Device Not Found")
