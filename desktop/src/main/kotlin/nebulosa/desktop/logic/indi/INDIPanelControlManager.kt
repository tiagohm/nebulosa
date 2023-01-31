package nebulosa.desktop.logic.indi

import io.reactivex.rxjava3.disposables.Disposable
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.logic.EventBus
import nebulosa.desktop.view.indi.INDIPanelControlView
import nebulosa.indi.device.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

class INDIPanelControlManager(private val view: INDIPanelControlView) : KoinComponent {

    private val equipmentManager by inject<EquipmentManager>()
    private val cacheProperties = HashMap<Device, HashMap<String, INDIPanelControlWindow.GroupPropertyVector>>()
    private val groups = ArrayList<INDIPanelControlWindow.Group>()
    private val logText = StringBuilder(1000 * 150)
    private val subscribers = arrayOfNulls<Disposable>(1)

    @JvmField val devices = SimpleListProperty(FXCollections.observableArrayList<Device>())

    private fun onEvent(event: DeviceEvent<*>) {
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

        if (device in attachedDevices) view.device = device
        else view.device = attachedDevices.firstOrNull()

        subscribers[0]?.dispose()
        subscribers[0] = EventBus.DEVICE
            .subscribe(filter = { it.device === view.device }, observeOnJavaFX = true, next = ::onEvent)
    }

    fun makeLog() {
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

        System.gc()
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

    private object GroupNameComparator : Comparator<String> {

        override fun compare(a: String, b: String): Int {
            return if (a == b) 0
            else if (a == "Main Control") -1
            else if (b == "Main Control") 1
            else a.compareTo(b)
        }
    }
}
