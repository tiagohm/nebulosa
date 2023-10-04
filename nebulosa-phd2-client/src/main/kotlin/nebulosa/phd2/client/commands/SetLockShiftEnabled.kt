package nebulosa.phd2.client.commands

data class SetLockShiftEnabled(val enabled: Boolean) : PHD2Command<Int> {

    override val methodName = "set_lock_shift_enabled"

    override val params = listOf(enabled)

    override val responseType = Int::class.java
}
