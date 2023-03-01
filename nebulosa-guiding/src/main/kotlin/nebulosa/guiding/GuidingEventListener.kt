package nebulosa.guiding

fun interface GuidingEventListener {

    fun onEvent(guider: Guider, event: GuidingEvent)
}
