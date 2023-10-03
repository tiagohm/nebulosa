package nebulosa.phd2.client.commands

import nebulosa.phd2.client.events.State

data object GetAppState : PHD2Command<State> {

    override val methodName = "get_app_state"

    override val params = null

    override val responseType = State::class.java
}
