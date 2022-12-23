package nebulosa.eventbus

interface Event {

    fun properties(): MutableMap<String, Any?>
}
