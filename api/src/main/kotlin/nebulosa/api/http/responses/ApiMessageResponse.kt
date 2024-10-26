package nebulosa.api.http.responses

import nebulosa.api.notification.Severity

data class ApiMessageResponse(
    @JvmField val type: Severity,
    @JvmField val message: String,
) {

    companion object {

        fun success(message: String) = ApiMessageResponse(Severity.SUCCESS, message)

        fun info(message: String) = ApiMessageResponse(Severity.INFO, message)

        fun warn(message: String) = ApiMessageResponse(Severity.WARNING, message)

        fun error(message: String) = ApiMessageResponse(Severity.ERROR, message)
    }
}
