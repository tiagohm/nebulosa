package nebulosa.log

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> loggerFor() = loggerFor(T::class.java)

@Suppress("NOTHING_TO_INLINE")
inline fun loggerFor(type: Class<*>): Logger = LoggerFactory.getLogger(type) as Logger

@Suppress("NOTHING_TO_INLINE")
inline fun loggerFor(name: String): Logger = LoggerFactory.getLogger(name) as Logger

inline fun Logger.info(vararg args: Any?, lazy: () -> String) {
    if (isInfoEnabled) {
        info(lazy(), *args)
    }
}

inline fun Logger.warn(vararg args: Any?, lazy: () -> String) {
    if (isWarnEnabled) {
        warn(lazy(), *args)
    }
}

inline fun Logger.error(vararg args: Any?, lazy: () -> String) {
    if (isErrorEnabled) {
        error(lazy(), *args)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun Logger.error(e: Throwable) {
    if (isErrorEnabled) {
        error(e.message, e)
    }
}

inline fun Logger.debug(vararg args: Any?, lazy: () -> String) {
    if (isDebugEnabled) {
        debug(lazy(), *args)
    }
}

inline fun Logger.trace(vararg args: Any?, lazy: () -> String) {
    if (isTraceEnabled) {
        trace(lazy(), *args)
    }
}
