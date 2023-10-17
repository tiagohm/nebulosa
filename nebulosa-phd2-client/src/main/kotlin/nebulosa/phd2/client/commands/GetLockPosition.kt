package nebulosa.phd2.client.commands

data object GetLockPosition : PHD2Command<IntArray> {

    override val methodName = "get_lock_position"

    override val params = null

    override val responseType = IntArray::class.java
}
