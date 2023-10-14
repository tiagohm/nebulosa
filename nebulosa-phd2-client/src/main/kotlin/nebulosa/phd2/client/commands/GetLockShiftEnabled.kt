package nebulosa.phd2.client.commands

data object GetLockShiftEnabled : PHD2Command<Boolean> {

    override val methodName = "get_lock_shift_enabled"

    override val params = null

    override val responseType = Boolean::class.java
}
