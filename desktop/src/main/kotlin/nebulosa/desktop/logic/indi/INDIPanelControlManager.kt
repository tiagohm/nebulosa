package nebulosa.desktop.logic.indi

import javafx.animation.PauseTransition
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.util.Duration
import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.helper.runBlockingMain
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.indi.INDIPanelControlView
import nebulosa.indi.device.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.Closeable
import java.util.*

@Component
class INDIPanelControlManager(private val view: INDIPanelControlView) : Closeable {

    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var eventBus: EventBus

    private val cacheProperties = HashMap<Device, HashMap<String, INDIPanelControlWindow.GroupPropertyVector>>()
    private val groups = ArrayList<INDIPanelControlWindow.Group>()
    private val logText = StringBuilder(1000 * 150)
    private val logTextDelay = PauseTransition(Duration.seconds(5.0))

    val devices = SimpleListProperty(FXCollections.observableArrayList<Device>())

    fun initialize() {
        eventBus.register(this)

        logTextDelay.setOnFinished { makeLog() }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onEvent(event: DeviceEvent<*>): Unit = runBlockingMain {
        if (event.device !== view.device) return@runBlockingMain

        when (event) {
            is DevicePropertyChanged -> {
                synchronized(cacheProperties) {
                    val container = cacheProperties[event.device]!![event.property.name]

                    if (container != null) {
                        container.update(event.property)
                    } else {
                        val group = groups.firstOrNull { it.name == event.property.group }

                        group?.add(event.property)
                            ?: view.makeGroup(event.property.group, listOf(event.property))
                                .also(groups::add)
                    }
                }
            }
            is DevicePropertyDeleted -> {
                synchronized(cacheProperties) {
                    val container = cacheProperties[event.device]!![event.property.name]
                    container?.delete()
                    cacheProperties[event.device]!!.remove(event.property.name)
                }
            }
            is DeviceMessageReceived -> {
                logTextDelay.playFromStart()
            }
        }
    }

    fun clear(device: Device) {
        view.clearTabs()
        cacheProperties[device]!!.clear()
        groups.clear()
    }

    fun populate() {
        val device = view.device
        val attachedDevices = ArrayList<Device>()
        attachedDevices.addAll(equipmentManager.attachedCameras)
        attachedDevices.addAll(equipmentManager.attachedMounts)
        attachedDevices.addAll(equipmentManager.attachedFilterWheels)
        attachedDevices.addAll(equipmentManager.attachedFocusers)
        attachedDevices.addAll(equipmentManager.attachedGPSs)
        attachedDevices.sortBy { it.name }
        attachedDevices.forEach { if (it !in cacheProperties) cacheProperties[it] = HashMap(256) }
        devices.setAll(attachedDevices)

        view.show((if (device in attachedDevices) device else attachedDevices.firstOrNull()) ?: return)
    }

    private fun makeLog() {
        logText.clear()
        view.device?.messages?.forEach(logText::appendLine)
        view.updateLog("$logText")
    }

    fun makePanelControl() {
        synchronized(cacheProperties) {
            val device = view.device ?: return

            clear(device)

            val groupedProperties = TreeMap<String, MutableList<PropertyVector<*, *>>>(GroupNameComparator)

            for (property in device.properties.values) {
                groupedProperties
                    .getOrPut(property.group) { ArrayList() }
                    .add(property)
            }

            groupedProperties
                .onEach { view.makeGroup(it.key, it.value).also(groups::add) }
        }

        makeLog()
    }

    fun removeGroup(group: INDIPanelControlWindow.Group) {
        groups.remove(group)
    }

    fun addGroupPropertyVector(
        device: Device, vector: PropertyVector<*, *>,
        container: INDIPanelControlWindow.GroupPropertyVector,
    ) {
        cacheProperties[device]!![vector.name] = container
    }

    override fun close() {
        eventBus.unregister(this)
    }

    private object GroupNameComparator : Comparator<String> {

        override fun compare(a: String, b: String): Int {
            return if (a == b) 0
            else if (a == "Main Control") -1
            else if (b == "Main Control") 1
            else a.compareTo(b)
        }
    }
}
