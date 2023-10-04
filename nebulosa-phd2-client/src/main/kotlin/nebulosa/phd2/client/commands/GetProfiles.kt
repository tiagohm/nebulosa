package nebulosa.phd2.client.commands

data object GetProfiles : PHD2Command<Array<Profile>> {

    override val methodName = "get_profiles"

    override val params = null

    override val responseType = Array<Profile>::class.java
}
