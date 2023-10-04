package nebulosa.phd2.client.commands

data object GetLockShiftParams : PHD2Command<LockShiftParams> {

    override val methodName = "get_lock_shift_params"

    override val params = null

    override val responseType = LockShiftParams::class.java
}
