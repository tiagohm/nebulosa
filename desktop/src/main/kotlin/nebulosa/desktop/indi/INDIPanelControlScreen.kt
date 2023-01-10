package nebulosa.desktop.indi

import io.reactivex.rxjava3.disposables.Disposable
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import nebulosa.desktop.core.beans.onZero
import nebulosa.desktop.core.scene.MaterialColor
import nebulosa.desktop.core.scene.MaterialIcon
import nebulosa.desktop.core.scene.Screen
import nebulosa.desktop.core.util.DeviceStringConverter
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.indi.devices.*
import nebulosa.indi.protocol.PropertyPermission
import nebulosa.indi.protocol.SwitchRule
import org.koin.core.component.inject
import java.util.*
import kotlin.math.min

class INDIPanelControlScreen : Screen("INDIPanelControl", "nebulosa-indi") {

    private val equipmentManager by inject<EquipmentManager>()

    @FXML private lateinit var devices: ChoiceBox<Device>
    @FXML private lateinit var tabs: TabPane
    @FXML private lateinit var logs: TextArea

    private val cacheProperties = HashMap<Device, HashMap<String, GroupPropertyVector>>()
    private val groups = ArrayList<Group>()
    private val subscribers = arrayOfNulls<Disposable>(2)
    private val logText = StringBuilder(1000 * 150)

    init {
        title = "INDI Panel Control"
    }

    override fun onCreate() {
        devices.converter = DeviceStringConverter()
        devices.selectionModel.selectedItemProperty().onZero(::makePanelControl)

        equipmentManager.attachedCameras.onZero(::populateDevices)
        equipmentManager.attachedMounts.onZero(::populateDevices)
        equipmentManager.attachedFilterWheels.onZero(::populateDevices)
        equipmentManager.attachedFocusers.onZero(::populateDevices)
    }

    override fun onStart() {
        subscribers[0] = eventBus
            .filterIsInstance<DevicePropertyEvent> { it.device === devices.value }
            .subscribe(::onEvent)

        subscribers[1] = eventBus
            .filterIsInstance<DeviceMessageReceived> { it.device === devices.value }
            .subscribe(::onEvent)

        populateDevices()
    }

    override fun onStop() {
        subscribers[0]?.dispose()
        subscribers[1]?.dispose()
        subscribers.fill(null)
    }

    fun select(device: Device): Boolean {
        return if (device in devices.items) {
            devices.selectionModel.select(device)
            true
        } else {
            false
        }
    }

    private fun onEvent(event: DevicePropertyEvent) {
        when (event) {
            is DevicePropertyChanged -> Platform.runLater {
                synchronized(cacheProperties) {
                    val container = cacheProperties[event.device]!![event.property.name]

                    if (container != null) {
                        container.update(event.property)
                    } else {
                        val group = groups.firstOrNull { it.name == event.property.group }

                        if (group != null) {
                            group.add(event.property)
                        } else {
                            tabs.makeGroup(event.property.group, listOf(event.property))
                        }
                    }
                }
            }
            is DevicePropertyDeleted -> Platform.runLater {
                synchronized(cacheProperties) {
                    val container = cacheProperties[event.device]!![event.property.name]
                    container?.delete()
                    cacheProperties[event.device]!!.remove(event.property.name)
                }
            }
        }
    }

    private fun onEvent(event: DeviceMessageReceived) {
        Platform.runLater {
            synchronized(logText) {
                logText.insert(0, "${event.message}\n")
                logs.text = logText.toString()
            }
        }
    }

    private fun populateDevices() {
        val device = devices.value
        val attachedDevices = ArrayList<Device>()
        attachedDevices.addAll(equipmentManager.attachedCameras)
        attachedDevices.addAll(equipmentManager.attachedMounts)
        attachedDevices.addAll(equipmentManager.attachedFilterWheels)
        attachedDevices.addAll(equipmentManager.attachedFocusers)
        attachedDevices.sortBy { it.name }
        attachedDevices.forEach { if (it !in cacheProperties) cacheProperties[it] = HashMap(256) }

        Platform.runLater {
            devices.items.setAll(attachedDevices)
            if (device in attachedDevices) devices.value = device
            else devices.selectionModel.selectFirst()
        }
    }

    private fun makePanelControl() {
        val device = devices.value ?: return

        synchronized(cacheProperties) {
            cacheProperties[device]!!.clear()
            tabs.tabs.clear()
            groups.clear()

            val groupedProperties = TreeMap<String, MutableList<PropertyVector<*, *>>>(GroupNameComparator)

            for (property in device.properties.values) {
                groupedProperties
                    .getOrPut(property.group) { ArrayList() }
                    .add(property)
            }

            groupedProperties
                .onEach { tabs.makeGroup(it.key, it.value) }
        }

        synchronized(logText) {
            logText.clear()
            device.messages.forEach(logText::appendLine)
            logs.text = logText.toString()
        }

        System.gc()
    }

    private fun TabPane.makeGroup(name: String, vectors: List<PropertyVector<*, *>>) {
        val tab = Tab()

        tab.text = name
        tab.isClosable = false

        val group = Group(tab, name, vectors)
        val scroll = ScrollPane(group)
        tab.content = scroll

        tabs.add(tab)
        groups.add(group)
    }

    private inner class Group(
        @JvmField val tab: Tab,
        @JvmField val name: String,
        @JvmField val vectors: List<PropertyVector<*, *>>,
    ) : VBox() {

        init {
            spacing = 8.0
            alignment = Pos.CENTER_LEFT
            styleClass.addAll("group")

            vectors.forEach(::add)
        }

        fun add(vector: PropertyVector<*, *>) {
            val device = devices.value!!
            val container = GroupPropertyVector(vector)
            children.add(container)
            cacheProperties[device]!![vector.name] = container
        }
    }

    private inner class GroupPropertyVector(vector: PropertyVector<*, *>) : HBox() {

        init {
            alignment = Pos.CENTER_LEFT
            styleClass.addAll("vector", "s-md")

            // Label.
            val name = Label("${vector.label}:").withState(vector)
            name.styleClass.add("text-md")
            name.prefWidth = 192.0
            name.minWidth = 192.0
            name.maxWidth = 192.0
            children.add(name)

            // Properties.
            val properties = GroupPropertyList(vector)
            children.add(properties)

            // Send button.
            if (vector !is SwitchPropertyVector && vector.perm != PropertyPermission.RO) {
                val send = Button("Send")
                send.cursor = Cursor.HAND
                send.minHeight = 26.0

                send.setOnAction {
                    val p = properties.children[0]

                    if (p is NumberGroupProperty) {
                        sendNumberPropertyVectorMessage(p.vector, p.inputs())
                    } else if (p is TextGroupProperty) {
                        sendTextPropertyVectorMessage(p.vector, p.inputs())
                    }
                }

                val icon = Label(MaterialIcon.SEND)
                icon.textFill = MaterialColor.BLUE_700
                icon.styleClass.addAll("mdi", "mdi-sm")
                send.graphic = icon

                children.add(send)
            }
        }

        fun update(vector: PropertyVector<*, *>) {
            isVisible = true
            isManaged = true

            val label = children[0] as Label
            label.updateState(vector)

            val properties = children[1] as GroupPropertyList
            properties.update(vector)

            // TODO: Can permission be updated?
        }

        fun delete() {
            isVisible = false
            isManaged = false

            val group = parent as Group
            val shouldBeRemoved = group.children.all { !it.isVisible }

            if (shouldBeRemoved) {
                group.tab.tabPane?.tabs?.remove(group.tab)
                groups.remove(group)
            }
        }

        private fun sendNumberPropertyVectorMessage(
            vector: NumberPropertyVector,
            data: Array<Pair<String, Double>>,
        ) {
            if (vector.perm == PropertyPermission.RO) return

            val device = devices.value!!
            device.sendNewNumber(vector.name, *data)
        }

        private fun sendTextPropertyVectorMessage(
            vector: TextPropertyVector,
            data: Array<Pair<String, String>>,
        ) {
            if (vector.perm == PropertyPermission.RO) return

            val device = devices.value!!
            device.sendNewText(vector.name, *data)
        }
    }

    private inner class GroupPropertyList(vector: PropertyVector<*, *>) : VBox() {

        init {
            spacing = 4.0
            alignment = Pos.CENTER_LEFT

            when (vector) {
                is SwitchPropertyVector -> children.add(SwitchGroupProperty(vector))
                is NumberPropertyVector -> children.add(NumberGroupProperty(vector))
                is TextPropertyVector -> children.add(TextGroupProperty(vector))
            }
        }

        fun update(vector: PropertyVector<*, *>) {
            when (vector) {
                is SwitchPropertyVector -> (children[0] as SwitchGroupProperty).update(vector)
                is NumberPropertyVector -> (children[0] as NumberGroupProperty).update(vector)
                is TextPropertyVector -> (children[0] as TextGroupProperty).update(vector)
            }
        }
    }

    private inner class SwitchGroupProperty(vector: SwitchPropertyVector) : HBox() {

        init {
            spacing = 2.0
            alignment = Pos.CENTER_LEFT

            vector.values.forEach { makeButton(vector, it) }
        }

        fun update(vector: SwitchPropertyVector) {
            val size = min(children.size, vector.values.size)
            val property = vector.values.iterator()

            repeat(size) { (children[it] as Button).updateButton(vector, property.next()) }
            repeat(vector.size - children.size) { makeButton(vector, property.next()) }
            repeat(children.size - vector.size) { children.removeAt(children.size - 1) }
        }

        private fun makeButton(
            vector: SwitchPropertyVector,
            property: SwitchProperty,
        ) {
            val button = Button(property.label)
            button.cursor = Cursor.HAND
            button.styleClass.addAll("text-md", "bold")
            button.setOnAction { sendSwitchPropertyVectorMessage(vector, property) }
            button.updateButton(vector, property)
            children.add(button)
        }

        private fun Button.updateButton(
            vector: SwitchPropertyVector,
            property: SwitchProperty,
        ) {
            isDisable = vector.perm == PropertyPermission.RO
            text = property.label
            textFill = if (property.value) MaterialColor.GREEN_700 else MaterialColor.GREY_800
        }

        private fun sendSwitchPropertyVectorMessage(
            vector: SwitchPropertyVector,
            property: SwitchProperty,
        ) {
            if (vector.perm == PropertyPermission.RO) return

            val device = devices.value!!

            if (vector.rule == SwitchRule.ANY_OF_MANY) {
                device.sendNewSwitch(vector.name, property.name to !property.value)
            } else {
                device.sendNewSwitch(vector.name, property.name to true)
            }
        }
    }

    private inner class NumberGroupProperty(@Volatile @JvmField var vector: NumberPropertyVector) : VBox() {

        init {
            spacing = 4.0
            alignment = Pos.CENTER_LEFT

            update(vector)
        }

        fun update(vector: NumberPropertyVector) {
            this.vector = vector

            val size = min(children.size, vector.values.size)
            val property = vector.values.iterator()

            repeat(size) { (children[it] as NumberGroupPropertyItem).update(vector, property.next()) }
            repeat(vector.size - children.size) { children.add(NumberGroupPropertyItem(vector, property.next())) }
            repeat(children.size - vector.size) { children.removeAt(children.size - 1) }
        }

        fun inputs() = Array(children.size) {
            val item = children[it] as NumberGroupPropertyItem
            item.property.name to (item.input!!.text.trim().toDoubleOrNull() ?: 0.0)
        }
    }

    private inner class NumberGroupPropertyItem(
        @Volatile @JvmField var vector: NumberPropertyVector,
        @Volatile @JvmField var property: NumberProperty,
    ) : HBox() {

        @JvmField val input: TextField?

        init {
            spacing = 2.0
            alignment = Pos.CENTER_LEFT

            // Label.
            val label = Label("${property.label}:")
            label.minWidth = 175.0
            label.maxWidth = 175.0
            label.prefWidth = 175.0
            label.styleClass.addAll("text-md", "bold")
            children.add(label)

            // Value.
            val value = TextField("%.8f".format(property.value))
            value.isDisable = true
            children.add(value)

            // Input.
            if (vector.perm != PropertyPermission.RO) {
                input = TextField("%.08f".format(property.value))
                input.addEventFilter(KeyEvent.KEY_TYPED, ::onKeyTyped)
                children.add(input)
            } else {
                input = null
            }
        }

        fun update(
            vector: NumberPropertyVector,
            property: NumberProperty,
        ) {
            this.vector = vector
            this.property = property

            val label = children[0] as Label
            label.text = "${property.label}:"

            val value = children[1] as TextField
            value.text = "%.8f".format(property.value)
        }

        private fun onKeyTyped(event: KeyEvent) {
            try {
                val text = "${input!!.text}${event.character}".trim()
                if (text == "-") return
                text.toDouble()
            } catch (_: NumberFormatException) {
                event.consume()
            }
        }
    }

    private inner class TextGroupProperty(@Volatile @JvmField var vector: TextPropertyVector) : VBox() {

        init {
            spacing = 4.0
            alignment = Pos.CENTER_LEFT

            update(vector)
        }

        fun update(vector: TextPropertyVector) {
            this.vector = vector

            val size = min(children.size, vector.values.size)
            val property = vector.values.iterator()

            repeat(size) { (children[it] as TextGroupPropertyItem).update(vector, property.next()) }
            repeat(vector.size - children.size) { children.add(TextGroupPropertyItem(vector, property.next())) }
            repeat(children.size - vector.size) { children.removeAt(children.size - 1) }
        }

        fun inputs() = Array(children.size) {
            val item = children[it] as TextGroupPropertyItem
            item.property.name to (item.input!!.text?.trim() ?: "")
        }
    }

    private inner class TextGroupPropertyItem(
        @Volatile @JvmField var vector: TextPropertyVector,
        @Volatile @JvmField var property: TextProperty,
    ) : HBox() {

        @JvmField val input: TextField?

        init {
            spacing = 2.0
            alignment = Pos.CENTER_LEFT

            // Label.
            val label = Label("${property.label}:")
            label.minWidth = 175.0
            label.maxWidth = 175.0
            label.prefWidth = 175.0
            label.styleClass.addAll("text-md", "bold")
            children.add(label)

            // Value.
            val value = TextField(property.value)
            value.isDisable = true
            children.add(value)

            // Input.
            if (vector.perm != PropertyPermission.RO) {
                input = TextField(property.value)
                children.add(input)
            } else {
                input = null
            }
        }

        fun update(
            vector: TextPropertyVector,
            property: TextProperty,
        ) {
            this.vector = vector
            this.property = property

            val label = children[0] as Label
            label.text = "${property.label}:"

            val value = children[1] as TextField
            value.text = property.value.trim()
        }
    }

    private object GroupNameComparator : Comparator<String> {

        override fun compare(a: String, b: String): Int {
            return if (a == b) 0
            else if (a == "Main Control") -1
            else if (b == "Main Control") 1
            else a.compareTo(b)
        }
    }

    companion object {

        @JvmStatic private val STATE_COLORS =
            arrayOf(MaterialColor.GREY_400, MaterialColor.GREEN_400, MaterialColor.BLUE_400, MaterialColor.RED_400)

        @JvmStatic
        private fun Label.withState(vector: PropertyVector<*, *>) = apply {
            val icon = Label(MaterialIcon.CIRCLE)
            icon.styleClass.addAll("mdi", "mdi-xs")
            graphic = icon
            updateState(vector)
        }

        @JvmStatic
        private fun Label.updateState(vector: PropertyVector<*, *>) = apply {
            (graphic as Label).textFill = STATE_COLORS[vector.state.ordinal]
        }
    }
}
