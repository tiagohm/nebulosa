package nebulosa.json

import com.fasterxml.jackson.databind.module.SimpleModule

@Suppress("NOTHING_TO_INLINE")
inline fun <T> SimpleModule.addSerializer(serializer: ToJson<T>) = apply {
    addSerializer(serializer.type, ToJsonSerializer(serializer))
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> SimpleModule.addDeserializer(deserializer: FromJson<T>) = apply {
    addDeserializer(deserializer.type, FromJsonDeserializer(deserializer))
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> SimpleModule.addConverter(converter: T) where T : FromJson<*>, T : ToJson<*> = apply {
    addSerializer(converter)
    addDeserializer(converter)
}
