package nebulosa.phd2.client.commands

import nebulosa.guiding.GuideState

data object GetAppState : PHD2Command<GuideState> {

    override val methodName = "get_app_state"

    override val params = null

    override val responseType = GuideState::class.java
}
