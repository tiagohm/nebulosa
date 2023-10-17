package nebulosa.phd2.client.commands

/**
 * Select an equipment profile. All equipment must be disconnected before switching profiles.
 */
data class SetProfile(val profile: Profile) : PHD2Command<Int> {

    override val methodName = "set_profile"

    override val params = listOf(profile.id)

    override val responseType = Int::class.java
}
