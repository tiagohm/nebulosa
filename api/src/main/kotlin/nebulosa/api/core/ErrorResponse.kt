package nebulosa.api.core

import nebulosa.api.notification.Severity

data class ErrorResponse(
    @JvmField val type: Severity,
    @JvmField val message: String,
) {

    companion object {

        fun success(message: String) = ErrorResponse(Severity.SUCCESS, message)

        fun info(message: String) = ErrorResponse(Severity.INFO, message)

        fun warn(message: String) = ErrorResponse(Severity.WARNING, message)

        fun error(message: String) = ErrorResponse(Severity.ERROR, message)
    }
}
