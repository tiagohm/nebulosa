@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.log

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> loggerFor() = loggerFor(T::class.java)

inline fun loggerFor(type: Class<*>) = LoggerFactory.getLogger(type) as Logger

inline fun loggerFor(name: String) = LoggerFactory.getLogger(name) as Logger

inline fun Logger.d(block: Logger.() -> Unit) {
    if (isDebugEnabled) block()
}
