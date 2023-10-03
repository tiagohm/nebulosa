package nebulosa.phd2.client.commands

// https://github.com/OpenPHDGuiding/phd2/wiki/EventMonitoring#phd-server-method-invocation

sealed interface PHD2Command<out T> {

    val methodName: String

    val params: Any?

    val responseType: Class<out T>?
}
