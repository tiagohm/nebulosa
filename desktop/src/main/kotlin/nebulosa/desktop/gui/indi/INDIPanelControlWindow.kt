package nebulosa.desktop.gui.indi

import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.util.StringConverter
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.indi.INDIPanelControlManager
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.toggle
import nebulosa.desktop.view.indi.INDIPanelControlView
import nebulosa.indi.device.*
import nebulosa.indi.protocol.PropertyPermission
import nebulosa.indi.protocol.SwitchRule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import kotlin.math.min

@Component
class INDIPanelControlWindow : AbstractWindow("INDIPanelControl", "nebulosa-indi"), INDIPanelControlView {

    @Lazy @Autowired private lateinit var indiPanelControlManager: INDIPanelControlManager

    @FXML private lateinit var deviceChoiceBox: ChoiceBox<Device>
    @FXML private lateinit var groupsTabPane: TabPane
    @FXML private lateinit var logTextArea: TextArea

    init {
        title = "INDI Panel Control"
    }

    override fun onCreate() {
        deviceChoiceBox.converter = DeviceStringConverter
        deviceChoiceBox.itemsProperty().bind(indiPanelControlManager.devices)
        deviceChoiceBox.selectionModel.selectedItemProperty().on { indiPanelControlManager.makePanelControl() }
    }

    override fun onStart() {
        indiPanelControlManager.populate()
    }

    override var device: Device?
        get() = deviceChoiceBox.value
        set(value) {
            deviceChoiceBox.value = value
        }

    val tabs: MutableList<Tab>
        get() = groupsTabPane.tabs

    override fun updateLog(text: String) {
        logTextArea.text = text
    }

    override fun clearTabs() {
        tabs.clear()
    }

    override fun makeGroup(name: String, vectors: List<PropertyVector<*, *>>): Group {
        val tab = Tab()

        tab.text = name
        tab.isClosable = false

        val group = Group(tab, name, vectors)
        val scroll = ScrollPane(group)
        tab.content = scroll

        tabs.add(tab)

        return group
    }

    inner class Group(
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
            val container = GroupPropertyVector(vector)
            children.add(container)
            indiPanelControlManager.addGroupPropertyVector(device!!, vector, container)
        }
    }

    inner class GroupPropertyVector(vector: PropertyVector<*, *>) : HBox() {

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

                val icon = Label("󰒊")
                icon.styleClass.addAll("text-blue-700", "mdi", "mdi-sm")
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
                indiPanelControlManager.removeGroup(group)
            }
        }

        private fun sendNumberPropertyVectorMessage(
            vector: NumberPropertyVector,
            data: Iterable<Pair<String, Double>>,
        ) {
            if (vector.perm == PropertyPermission.RO) return

            device?.sendNewNumber(vector.name, data)
        }

        private fun sendTextPropertyVectorMessage(
            vector: TextPropertyVector,
            data: Iterable<Pair<String, String>>,
        ) {
            if (vector.perm == PropertyPermission.RO) return

            device?.sendNewText(vector.name, data)
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
            button.styleClass.addAll("text-md", "text-bold")
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
            styleClass.toggle("text-green-700", "text-grey-800", property.value)
        }

        private fun sendSwitchPropertyVectorMessage(
            vector: SwitchPropertyVector,
            property: SwitchProperty,
        ) {
            if (vector.perm == PropertyPermission.RO) return

            if (vector.rule == SwitchRule.ANY_OF_MANY) {
                device?.sendNewSwitch(vector.name, property.name to !property.value)
            } else {
                device?.sendNewSwitch(vector.name, property.name to true)
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

        fun inputs() = children.map {
            it as NumberGroupPropertyItem
            it.property.name to (it.input!!.text.trim().toDoubleOrNull() ?: 0.0)
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
            label.styleClass.addAll("text-md", "text-bold")
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

        fun inputs() = children.map {
            it as TextGroupPropertyItem
            it.property.name to (it.input!!.text?.trim() ?: "")
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
            label.styleClass.addAll("text-md", "text-bold")
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

    private object DeviceStringConverter : StringConverter<Device>() {

        override fun toString(device: Device?) = device?.name ?: "No device selected"

        override fun fromString(text: String?) = null
    }

    companion object {

        @JvmStatic private val STATE_COLORS = arrayOf("text-grey-400", "text-green-400", "text-blue-400", "text-red-400")

        @JvmStatic
        private fun Label.withState(vector: PropertyVector<*, *>) = apply {
            val icon = Label("󰝥")
            icon.styleClass.addAll("mdi", "mdi-xs")
            graphic = icon
            updateState(vector)
        }

        @JvmStatic
        private fun Label.updateState(vector: PropertyVector<*, *>) = apply {
            with(graphic as Label) {
                styleClass.removeAll(STATE_COLORS)
                styleClass.add(STATE_COLORS[vector.state.ordinal])
            }
        }
    }
}
