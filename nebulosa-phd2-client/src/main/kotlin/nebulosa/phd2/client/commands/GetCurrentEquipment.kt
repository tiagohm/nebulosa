package nebulosa.phd2.client.commands

data object GetCurrentEquipment : PHD2Command<Equipment> {

    override val methodName = "get_current_equipment"

    override val params = null

    override val responseType = Equipment::class.java
}
