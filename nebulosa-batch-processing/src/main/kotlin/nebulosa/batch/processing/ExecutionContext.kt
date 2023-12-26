package nebulosa.batch.processing

import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

open class ExecutionContext : ConcurrentHashMap<String, Any?> {

    constructor(initialCapacity: Int = 64) : super(initialCapacity)

    constructor(context: ExecutionContext) : super(context)

    companion object {

        @JvmStatic
        fun <T> ExecutionContext.getOrNull(key: String, type: Class<T>) = if (containsKey(key)) type.cast(this[key])
        else null

        @JvmStatic
        fun ExecutionContext.getBoolean(key: String, value: Boolean = false) = if (containsKey(key)) this[key] as Boolean
        else value

        @JvmStatic
        fun ExecutionContext.getBooleanOrNull(key: String) = if (containsKey(key)) this[key] as? Boolean
        else null

        @JvmStatic
        fun ExecutionContext.getInt(key: String, value: Int = 0) = if (containsKey(key)) this[key] as Int
        else value

        @JvmStatic
        fun ExecutionContext.getIntOrNull(key: String) = if (containsKey(key)) this[key] as? Int
        else null

        @JvmStatic
        fun ExecutionContext.getDouble(key: String, value: Double = 0.0) = if (containsKey(key)) this[key] as Double
        else value

        @JvmStatic
        fun ExecutionContext.getDoubleOrNull(key: String) = if (containsKey(key)) this[key] as? Double
        else null

        @JvmStatic
        fun ExecutionContext.getText(key: String, value: String = "") = if (containsKey(key)) this[key] as String
        else value

        @JvmStatic
        fun ExecutionContext.getTextOrNull(key: String) = if (containsKey(key)) this[key] as? String
        else null

        @JvmStatic
        fun ExecutionContext.getDuration(key: String, value: Duration = Duration.ZERO) = if (containsKey(key)) this[key] as Duration
        else value

        @JvmStatic
        fun ExecutionContext.getDurationOrNull(key: String) = if (containsKey(key)) this[key] as? Duration
        else null

        @JvmStatic
        fun ExecutionContext.getPath(key: String) = if (containsKey(key)) this[key] as? Path
        else null
    }
}
