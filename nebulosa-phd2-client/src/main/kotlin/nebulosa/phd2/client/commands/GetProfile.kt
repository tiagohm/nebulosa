package nebulosa.phd2.client.commands

data object GetProfile : PHD2Command<Profile> {

    override val methodName = "get_profile"

    override val params = null

    override val responseType = Profile::class.java
}
