package nebulosa.log

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> loggerFor() = loggerFor(T::class.java)

@Suppress("NOTHING_TO_INLINE")
inline fun loggerFor(type: Class<*>): Logger = LoggerFactory.getLogger(type) as Logger

@Suppress("NOTHING_TO_INLINE")
inline fun loggerFor(name: String): Logger = LoggerFactory.getLogger(name) as Logger

inline fun Logger.info(lazy: () -> String) {
    if (isInfoEnabled) {
        info(lazy())
    }
}

inline fun Logger.warn(lazy: () -> String) {
    if (isWarnEnabled) {
        warn(lazy())
    }
}

inline fun Logger.error(lazy: () -> String) {
    if (isErrorEnabled) {
        error(lazy())
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun Logger.error(e: Throwable) {
    if (isErrorEnabled) {
        error(e.message, e)
    }
}

inline fun Logger.error(e: Throwable, lazy: () -> String) {
    if (isErrorEnabled) {
        error(lazy(), e)
    }
}

inline fun Logger.debug(lazy: () -> String) {
    if (isDebugEnabled) {
        debug(lazy())
    }
}

inline fun Logger.trace(lazy: () -> String) {
    if (isTraceEnabled) {
        trace(lazy())
    }
}
