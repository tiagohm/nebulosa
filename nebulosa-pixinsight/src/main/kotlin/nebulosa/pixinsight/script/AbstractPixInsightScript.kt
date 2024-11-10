package nebulosa.pixinsight.script

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import nebulosa.json.PathModule
import nebulosa.log.di
import nebulosa.log.loggerFor
import org.apache.commons.codec.binary.Hex
import java.nio.file.Path
import kotlin.io.path.readText

abstract class AbstractPixInsightScript<T : PixInsightScriptOutput> : PixInsightScript<T> {

    override val name = this::class.simpleName!!

    companion object {

        internal const val START_FILE = "@"
        internal const val END_FILE = "#"

        private val LOG = loggerFor<AbstractPixInsightScript<*>>()

        internal val OBJECT_MAPPER = jsonMapper {
            addModule(PathModule())
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }

        internal fun PixInsightScript<*>.execute(scriptPath: Path, data: Any?, slot: Int = this.slot): String {
            LOG.di("{} will be executed. slot={}, script={}, data={}", name, slot, scriptPath, data)

            return buildString {
                if (slot > 0) append("$slot:")
                append("$scriptPath")

                if (data != null) {
                    append(',')

                    when (data) {
                        is Path, is CharSequence -> append("$data")
                        is Number -> append("$data")
                        else -> append(Hex.encodeHexString(OBJECT_MAPPER.writeValueAsString(data).toByteArray(Charsets.UTF_16BE)))
                    }
                }
            }
        }

        internal fun <T : PixInsightScriptOutput> Path.parseStatus(type: Class<T>): T? {
            val text = readText()

            return if (text.startsWith(START_FILE) && text.endsWith(END_FILE)) {
                OBJECT_MAPPER.readValue(text.substring(1, text.length - 1), type)
            } else {
                null
            }
        }

        internal inline fun <reified T : PixInsightScriptOutput> Path.parseStatus(): T? {
            return parseStatus(T::class.java)
        }
    }
}
