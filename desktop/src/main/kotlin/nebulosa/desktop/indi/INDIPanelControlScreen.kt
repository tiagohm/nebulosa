package nebulosa.desktop.indi

import io.reactivex.rxjava3.disposables.Disposable
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
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

class INDIPanelControlScreen : Screen("INDIPanelControl", "nebulosa-indi") {

    private val equipmentManager by inject<EquipmentManager>()

    @FXML private lateinit var devices: ChoiceBox<Device>
    @FXML private lateinit var panelControl: AnchorPane
    @FXML private lateinit var groups: TabPane

    private val cacheProperties = HashMap<Device, HashMap<String, HBox>>()

    @Volatile private var subscriberForChanged: Disposable? = null
    @Volatile private var subscriberForDeleted: Disposable? = null

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
        subscriberForChanged = eventBus
            .filterIsInstance<DevicePropertyChanged> { it.device === devices.value }
            .subscribe(::onEvent)

        subscriberForDeleted = eventBus
            .filterIsInstance<DevicePropertyDeleted> { it.device === devices.value }
            .subscribe(::onEvent)

        populateDevices()
    }

    override fun onStop() {
        subscriberForChanged?.dispose()
        subscriberForChanged = null
        subscriberForDeleted?.dispose()
        subscriberForDeleted = null
    }

    fun select(device: Device): Boolean {
        return if (device in devices.items) {
            devices.selectionModel.select(device)
            true
        } else {
            false
        }
    }

    private fun onEvent(event: DevicePropertyChanged) {
        val container = cacheProperties[event.device]!![event.property.name]

        if (container != null) {
            Platform.runLater { container.updateProperty(event.property) }
        } else {
            val tab = groups.tabs
                .firstOrNull { it.userData == event.property.group } ?: return
            val content = (tab.content as ScrollPane).content as VBox
            Platform.runLater { content.makeGroupProperty(event.device, event.property) }
        }
    }

    private fun onEvent(event: DevicePropertyDeleted) {
        val container = cacheProperties[event.device]!![event.property.name]

        Platform.runLater { container?.deleteProperty(event.property) }
    }

    private fun populateDevices() {
        val devices = ArrayList<Device>()
        devices.addAll(equipmentManager.attachedCameras)
        devices.addAll(equipmentManager.attachedMounts)
        devices.addAll(equipmentManager.attachedFilterWheels)
        devices.addAll(equipmentManager.attachedFocusers)
        devices.sortBy { it.name }
        devices.forEach { if (it !in cacheProperties) cacheProperties[it] = HashMap(256) }
        this.devices.items.setAll(devices)
    }

    @Synchronized
    private fun makePanelControl() {
        val device = devices.value ?: return

        cacheProperties[device]!!.clear()
        groups.tabs.clear()

        device.values
            .groupBy { it.group }
            .onEach { groups.makeGroup(device, it.key, it.value) }

        System.gc()
    }

    private fun TabPane.makeGroup(device: Device, name: String, properties: List<PropertyVector<*, *>>) {
        val tab = Tab()

        tab.text = name
        tab.isClosable = false
        tab.userData = name

        val container = VBox()

        container.spacing = 8.0
        container.padding = PADDING_16

        for (property in properties) {
            container.makeGroupProperty(device, property)
        }

        val scroll = ScrollPane(container)
        tab.content = scroll

        tabs.add(tab)
    }

    private val PropertyVector<*, *>.styleClassName
        get() = when (this) {
            is NumberPropertyVector -> "number"
            is SwitchPropertyVector -> "switch"
            is TextPropertyVector -> "text"
        }

    private fun VBox.makeGroupProperty(device: Device, property: PropertyVector<*, *>) {
        val propertyContainer = HBox()
        propertyContainer.alignment = Pos.CENTER_LEFT
        propertyContainer.spacing = 2.0
        propertyContainer.makeProperty(property)
        propertyContainer.styleClass.addAll("vector", property.styleClassName)
        children.add(propertyContainer)
        cacheProperties[device]!![property.name] = propertyContainer
    }

    private fun Label.makeState(vector: PropertyVector<*, *>) {
        val icon = Label(MaterialIcon.CIRCLE)
        icon.textFill = STATE_COLORS[vector.state.ordinal]
        icon.font = MATERIAL_DESIGN_ICONS_24
        graphic = icon
    }

    private fun HBox.makeProperty(vector: PropertyVector<*, *>) {
        children.clear()

        userData = vector

        val name = Label()
        name.text = "${vector.label}:"
        name.makeState(vector)
        name.minWidth = 192.0
        name.maxWidth = 192.0
        name.prefWidth = 192.0
        name.font = SYSTEM_REGULAR_11
        children.add(name)

        when (vector) {
            is SwitchPropertyVector -> makeSwitchProperty(vector)
            is TextPropertyVector -> makeTextProperty(vector)
            is NumberPropertyVector -> makeNumberProperty(vector)
        }
    }

    private fun HBox.updateProperty(vector: PropertyVector<*, *>) {
        if (!isVisible) {
            isVisible = true
            isManaged = true
        }

        when (vector) {
            is SwitchPropertyVector -> updateSwitchProperty(vector)
            is TextPropertyVector -> updateTextProperty(vector)
            is NumberPropertyVector -> updateNumberProperty(vector)
        }
    }

    private fun HBox.deleteProperty(vector: PropertyVector<*, *>) {
        isVisible = false
        isManaged = false
    }

    // SWITCH.

    private fun HBox.makeSwitchProperty(vector: SwitchPropertyVector) {
        for ((_, property) in vector) {
            makeSwitchButton(vector, property)
        }
    }

    private fun HBox.makeSwitchButton(vector: SwitchPropertyVector, property: SwitchProperty) {
        val button = Button(property.label)
        button.userData = property.name
        button.cursor = Cursor.HAND
        button.font = SYSTEM_BOLD_11
        button.isDisable = vector.perm == PropertyPermission.RO
        button.textFill = if (property.value) MaterialColor.GREEN_700 else MaterialColor.GREY_800
        button.setOnAction { sendSwitchPropertyVectorMessage(vector, property) }
        children.add(button)
    }

    private fun HBox.updateSwitchProperty(vector: SwitchPropertyVector) {
        for ((_, property) in vector) {
            val button = children.firstOrNull { it.userData == property.name } as? Button

            if (button == null) {
                makeSwitchButton(vector, property)
                continue
            }

            button.isDisable = vector.perm == PropertyPermission.RO
            button.textFill = if (property.value) MaterialColor.GREEN_700 else MaterialColor.GREY_800
        }
    }

    private fun sendSwitchPropertyVectorMessage(
        vector: SwitchPropertyVector,
        property: SwitchProperty,
    ) {
        if (vector.perm == PropertyPermission.RO) return

        val device = devices.value ?: return

        if (vector.rule == SwitchRule.ANY_OF_MANY) {
            device.sendNewSwitch(vector.name, property.name to !property.value)
        } else {
            device.sendNewSwitch(vector.name, property.name to true)
        }
    }

    // TEXT.

    private fun HBox.makeTextProperty(vector: TextPropertyVector) {
        val vectorContainer = VBox()

        vectorContainer.spacing = 2.0
        vectorContainer.alignment = Pos.CENTER
        vectorContainer.userData = vector.name

        val inputs = ArrayList<TextField>(vector.size)

        // One property per line.
        for ((_, property) in vector) {
            val input = vectorContainer.makeTextFields(vector, property) ?: continue
            inputs.add(input)
        }

        children.add(vectorContainer)

        // Send button.
        if (vector.perm != PropertyPermission.RO) {
            val send = Button("Send")
            send.cursor = Cursor.HAND
            send.minHeight = 22.0
            HBox.setMargin(send, PADDING_HORIZONTAL_16)
            send.setOnAction {
                val data = inputs.map { it.userData as String to it.text }
                sendTextPropertyVectorMessage(vector, data)
            }

            val icon = Label(MaterialIcon.SEND)
            icon.textFill = MaterialColor.BLUE_700
            icon.font = MATERIAL_DESIGN_ICONS_18
            send.graphic = icon

            children.add(send)
        }
    }

    private fun VBox.makeTextFields(vector: TextPropertyVector, property: TextProperty): TextField? {
        val propertyContainer = HBox()

        propertyContainer.spacing = 4.0
        propertyContainer.alignment = Pos.CENTER_LEFT
        propertyContainer.userData = property.name

        // Label.
        val label = Label("${property.label}:")
        label.font = SYSTEM_BOLD_11
        propertyContainer.children.add(label)

        // Value.
        val value = TextField(property.value)
        value.isDisable = true
        value.userData = property.name
        propertyContainer.children.add(value)

        // Input for new value.
        val input = if (vector.perm != PropertyPermission.RO) {
            val input = TextField(property.value)
            input.userData = property.name
            propertyContainer.children.add(input)
            input
        } else {
            null
        }

        children.add(propertyContainer)

        return input
    }

    private fun HBox.updateTextProperty(vector: TextPropertyVector) {
        val vectorContainer = children
            .firstOrNull { it.userData == vector.name } as? VBox ?: return

        for ((_, property) in vector) {
            val propertyContainer = vectorContainer.children
                .firstOrNull { it.userData == property.name } as? HBox

            if (propertyContainer == null) {
                vectorContainer.makeTextFields(vector, property)
                continue
            }

            val value = propertyContainer.children
                .firstOrNull { it.userData == property.name } as? TextField ?: continue

            value.text = property.value
        }
    }

    private fun sendTextPropertyVectorMessage(
        vector: TextPropertyVector,
        data: List<Pair<String, String>>,
    ) {
        if (vector.perm == PropertyPermission.RO) return

        val device = devices.value ?: return
        device.sendNewText(vector.name, *data.toTypedArray())
    }

    // NUMBER.

    private fun HBox.makeNumberProperty(vector: NumberPropertyVector) {
        val vectorContainer = VBox()

        vectorContainer.spacing = 2.0
        vectorContainer.alignment = Pos.CENTER
        vectorContainer.userData = vector.name

        val inputs = ArrayList<TextField>(vector.size)

        // One property per line.
        for ((_, property) in vector) {
            val input = vectorContainer.makeNumberFields(vector, property) ?: continue
            inputs.add(input)
        }

        children.add(vectorContainer)

        // Send button.
        if (vector.perm != PropertyPermission.RO) {
            val send = Button("Send")
            send.cursor = Cursor.HAND
            send.minHeight = 22.0
            HBox.setMargin(send, PADDING_HORIZONTAL_16)
            send.setOnAction {
                val data = inputs.map { it.userData as String to (it.text.trim().toDoubleOrNull() ?: 0.0) }
                sendNumberPropertyVectorMessage(vector, data)
            }

            val icon = Label(MaterialIcon.SEND)
            icon.textFill = MaterialColor.BLUE_700
            icon.font = MATERIAL_DESIGN_ICONS_18
            send.graphic = icon

            children.add(send)
        }
    }

    private fun VBox.makeNumberFields(vector: NumberPropertyVector, property: NumberProperty): TextField? {
        val propertyContainer = HBox()

        propertyContainer.spacing = 4.0
        propertyContainer.alignment = Pos.CENTER_LEFT
        propertyContainer.userData = property.name

        // Label.
        val label = Label("${property.label}:")
        label.font = SYSTEM_BOLD_11
        propertyContainer.children.add(label)

        // Value.
        val value = TextField("%.8f".format(property.value))
        value.isDisable = true
        value.userData = property.name
        propertyContainer.children.add(value)

        // Input for new value.
        val input = if (vector.perm != PropertyPermission.RO) {
            val input = TextField("%.08f".format(property.value))
            input.addEventFilter(KeyEvent.KEY_TYPED) {
                try {
                    val text = "${input.text}${it.character}".trim()
                    if (text == "-") return@addEventFilter
                    text.toDouble()
                } catch (_: NumberFormatException) {
                    it.consume()
                }
            }
            input.userData = property.name
            propertyContainer.children.add(input)
            input
        } else {
            null
        }

        children.add(propertyContainer)

        return input
    }

    private fun HBox.updateNumberProperty(vector: NumberPropertyVector) {
        val vectorContainer = children
            .firstOrNull { it.userData == vector.name } as? VBox ?: return

        for ((_, property) in vector) {
            val propertyContainer = vectorContainer.children
                .firstOrNull { it.userData == property.name } as? HBox

            if (propertyContainer == null) {
                vectorContainer.makeNumberFields(vector, property)
                continue
            }

            val value = propertyContainer.children
                .firstOrNull { it.userData == property.name } as? TextField ?: continue

            value.text = "%.8f".format(property.value)
        }
    }

    private fun sendNumberPropertyVectorMessage(
        vector: NumberPropertyVector,
        data: List<Pair<String, Double>>,
    ) {
        if (vector.perm == PropertyPermission.RO) return

        val device = devices.value ?: return
        device.sendNewNumber(vector.name, *data.toTypedArray())
    }

    companion object {

        @JvmStatic private val PADDING_16 = Insets(16.0)
        @JvmStatic private val PADDING_HORIZONTAL_16 = Insets(0.0, 16.0, 0.0, 16.0)
        @JvmStatic private val PADDING_VERTICAL_16 = Insets(16.0, 0.0, 16.0, 0.0)
        @JvmStatic private val SYSTEM_REGULAR_11 = Font("System Regular", 11.0)
        @JvmStatic private val SYSTEM_BOLD_11 = Font("System Bold", 11.0)
        @JvmStatic private val MATERIAL_DESIGN_ICONS_18 = Font("Material Design Icons", 18.0)
        @JvmStatic private val MATERIAL_DESIGN_ICONS_24 = Font("Material Design Icons", 24.0)
        @JvmStatic private val DECIMAL_NUMBER_REGEX = Regex("^-?\\d+(\\.\\d+)?\\$")

        @JvmStatic private val STATE_COLORS =
            arrayOf(MaterialColor.GREY_400, MaterialColor.GREEN_400, MaterialColor.BLUE_400, MaterialColor.RED_400)
    }
}
