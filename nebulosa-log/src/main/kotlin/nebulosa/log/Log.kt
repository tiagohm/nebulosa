@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.log

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> loggerFor() = loggerFor(T::class.java)

inline fun loggerFor(type: Class<*>) = LoggerFactory.getLogger(type) as Logger

inline fun loggerFor(name: String) = LoggerFactory.getLogger(name) as Logger

// INFO

inline fun Logger.i(message: String) {
    if (isInfoEnabled) info(message)
}

inline fun Logger.i(message: String, a0: Any?) {
    if (isInfoEnabled) info(message, a0)
}

inline fun Logger.i(message: String, a0: Any?, a1: Any?) {
    if (isInfoEnabled) info(message, a0, a1)
}

inline fun Logger.i(message: String, a0: Any?, a1: Any?, a2: Any?) {
    if (isInfoEnabled) info(message, a0, a1, a2)
}

// WARN

inline fun Logger.w(message: String) {
    if (isWarnEnabled) warn(message)
}

inline fun Logger.w(message: String, a0: Any?) {
    if (isWarnEnabled) warn(message, a0)
}

inline fun Logger.w(message: String, a0: Any?, a1: Any?) {
    if (isWarnEnabled) warn(message, a0, a1)
}

inline fun Logger.w(message: String, a0: Any?, a1: Any?, a2: Any?) {
    if (isWarnEnabled) warn(message, a0, a1, a2)
}

inline fun Logger.w(message: String, a0: Any?, a1: Any?, a2: Any?, a3: Any?) {
    if (isWarnEnabled) warn(message, a0, a1, a2, a3)
}

inline fun Logger.w(message: String, a0: Any?, a1: Any?, a2: Any?, a3: Any?, a4: Any?) {
    if (isWarnEnabled) warn(message, a0, a1, a2, a3, a4)
}

inline fun Logger.w(message: String, a0: Any?, a1: Any?, a2: Any?, a3: Any?, a4: Any?, a5: Any?) {
    if (isWarnEnabled) warn(message, a0, a1, a2, a3, a4, a5)
}

inline fun Logger.w(message: String, a0: Any?, a1: Any?, a2: Any?, a3: Any?, a4: Any?, a5: Any?, a6: Any?) {
    if (isWarnEnabled) warn(message, a0, a1, a2, a3, a4, a5, a6)
}

inline fun Logger.w(message: String, a0: Any?, a1: Any?, a2: Any?, a3: Any?, a4: Any?, a5: Any?, a6: Any?, a7: Any?) {
    if (isWarnEnabled) warn(message, a0, a1, a2, a3, a4, a5, a6, a7)
}

inline fun Logger.w(message: String, a0: Any?, a1: Any?, a2: Any?, a3: Any?, a4: Any?, a5: Any?, a6: Any?, a7: Any?, a8: Any?) {
    if (isWarnEnabled) warn(message, a0, a1, a2, a3, a4, a5, a6, a7, a8)
}

// ERROR

inline fun Logger.e(message: String, a0: Any?) {
    if (isErrorEnabled) error(message, a0)
}

inline fun Logger.e(message: String, a0: Any?, a1: Any?) {
    if (isErrorEnabled) error(message, a0, a1)
}

// DEBUG

inline fun Logger.d(message: String) {
    if (isDebugEnabled) debug(message)
}

inline fun Logger.d(message: String, a0: Any?) {
    if (isDebugEnabled) debug(message, a0)
}

inline fun Logger.d(message: String, a0: Any?, a1: Any?) {
    if (isDebugEnabled) debug(message, a0, a1)
}

inline fun Logger.d(message: String, a0: Any?, a1: Any?, a2: Any?) {
    if (isDebugEnabled) debug(message, a0, a1, a2)
}

inline fun Logger.d(message: String, a0: Any?, a1: Any?, a2: Any?, a3: Any?) {
    if (isDebugEnabled) debug(message, a0, a1, a2, a3)
}

inline fun Logger.d(message: String, a0: Any?, a1: Any?, a2: Any?, a3: Any?, a4: Any?) {
    if (isDebugEnabled) debug(message, a0, a1, a2, a3, a4)
}

inline fun Logger.d(message: String, a0: Any?, a1: Any?, a2: Any?, a3: Any?, a4: Any?, a5: Any?) {
    if (isDebugEnabled) debug(message, a0, a1, a2, a3, a4, a5)
}

// DEBUG ERROR

inline fun Logger.de(message: String, a0: Any?) {
    if (isDebugEnabled) error(message, a0)
}

// DEBUG WARN

inline fun Logger.dw(message: String) {
    if (isDebugEnabled) warn(message)
}

inline fun Logger.dw(message: String, a0: Any?) {
    if (isDebugEnabled) warn(message, a0)
}

inline fun Logger.dw(message: String, a0: Any?, a1: Any?) {
    if (isDebugEnabled) warn(message, a0, a1)
}

inline fun Logger.dw(message: String, a0: Any?, a1: Any?, a2: Any?) {
    if (isDebugEnabled) warn(message, a0, a1, a2)
}

// DEBUG INFO

inline fun Logger.di(message: String) {
    if (isDebugEnabled) info(message)
}

inline fun Logger.di(message: String, a0: Any?) {
    if (isDebugEnabled) info(message, a0)
}

inline fun Logger.di(message: String, a0: Any?, a1: Any?) {
    if (isDebugEnabled) info(message, a0, a1)
}

inline fun Logger.di(message: String, a0: Any?, a1: Any?, a2: Any?) {
    if (isDebugEnabled) info(message, a0, a1, a2)
}

inline fun Logger.di(message: String, a0: Any?, a1: Any?, a2: Any?, a3: Any?) {
    if (isDebugEnabled) info(message, a0, a1, a2, a3)
}

inline fun Logger.di(message: String, a0: Any?, a1: Any?, a2: Any?, a3: Any?, a4: Any?) {
    if (isDebugEnabled) info(message, a0, a1, a2, a3, a4)
}

inline fun Logger.di(message: String, a0: Any?, a1: Any?, a2: Any?, a3: Any?, a4: Any?, a5: Any?) {
    if (isDebugEnabled) info(message, a0, a1, a2, a3, a4, a5)
}

inline fun Logger.di(message: String, a0: Any?, a1: Any?, a2: Any?, a3: Any?, a4: Any?, a5: Any?, a6: Any?) {
    if (isDebugEnabled) info(message, a0, a1, a2, a3, a4, a5, a6)
}
