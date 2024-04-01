package nebulosa.indi.protocol.parser

import com.fasterxml.aalto.stax.InputFactoryImpl
import nebulosa.indi.protocol.*
import nebulosa.indi.protocol.io.INDIInputStream
import nebulosa.xml.attribute
import java.io.InputStream
import javax.xml.stream.XMLStreamConstants

class INDIXmlInputStream(source: InputStream) : INDIInputStream {

    private val reader = XML_INPUT_FACTORY.createXMLStreamReader(source)

    @Synchronized
    override fun readINDIProtocol() = parseXML()

    private fun parseXML(): INDIProtocol? {
        while (reader.hasNext()) {
            when (reader.next()) {
                XMLStreamConstants.START_ELEMENT -> {
                    return parseStartElement() ?: continue
                }
            }
        }

        return null
    }

    private fun parseStartElement(): INDIProtocol? {
        return when (reader.localName) {
            SET_SWITCH_VECTOR_NAME -> parseSetSwitchVector()
            SET_NUMBER_VECTOR_NAME -> parseSetNumberVector()
            SET_TEXT_VECTOR_NAME -> parseSetTextVector()
            SET_LIGHT_VECTOR_NAME -> parseSetLightVector()
            SET_BLOB_VECTOR_NAME -> parseSetBLOBVector()
            DEF_SWITCH_VECTOR_NAME -> parseDefSwitchVector()
            DEF_NUMBER_VECTOR_NAME -> parseDefNumberVector()
            DEF_TEXT_VECTOR_NAME -> parseDefTextVector()
            DEF_LIGHT_VECTOR_NAME -> parseDefLightVector()
            DEF_BLOB_VECTOR_NAME -> parseDefBLOBVector()
            NEW_SWITCH_VECTOR_NAME -> parseNewSwitchVector()
            NEW_NUMBER_VECTOR_NAME -> parseNewNumberVector()
            NEW_TEXT_VECTOR_NAME -> parseNewTextVector()
            NEW_LIGHT_VECTOR_NAME -> parseNewLightVector()
            NEW_BLOB_VECTOR_NAME -> parseNewBLOBVector()
            GET_PROPERTIES_NAME -> parseGetProperties()
            DEL_PROPERTY_NAME -> parseDelProperty()
            MESSAGE_NAME -> parseMessage()
            ENABLE_BLOB_NAME -> parseEnableBLOB()
            else -> null
        }
    }

    // VECTOR.

    private fun DefVector<*>.parseDefVector() {
        device = reader.attribute(DEVICE_ATTR_NAME)!!
        name = reader.attribute(NAME_ATTR_NAME)!!
        label = reader.attribute(LABEL_ATTR_NAME)!!
        group = reader.attribute(GROUP_ATTR_NAME)!!
        state = PropertyState.parse(reader.attribute(STATE_ATTR_NAME)!!)
        reader.attribute(PERM_ATTR_NAME)?.also { perm = PropertyPermission.parse(it) }
        reader.attribute(TIMEOUT_ATTR_NAME)?.toDoubleOrNull()?.also { timeout = it }
        timestamp = reader.attribute(TIMESTAMP_ATTR_NAME)!!
    }

    private fun parseDefSwitchVector(): DefSwitchVector {
        val vector = DefSwitchVector()
        vector.parseDefVector()
        vector.rule = SwitchRule.parse(reader.attribute(RULE_ATTR_NAME)!!)
        vector.parseVectorElements(::parseDefSwitch)
        return vector
    }

    private fun parseDefNumberVector(): DefNumberVector {
        val vector = DefNumberVector()
        vector.parseDefVector()
        vector.parseVectorElements(::parseDefNumber)
        return vector
    }

    private fun parseDefTextVector(): DefTextVector {
        val vector = DefTextVector()
        vector.parseDefVector()
        vector.parseVectorElements(::parseDefText)
        return vector
    }

    private fun parseDefLightVector(): DefLightVector {
        val vector = DefLightVector()
        vector.parseDefVector()
        vector.parseVectorElements(::parseDefLight)
        return vector
    }

    private fun parseDefBLOBVector(): DefBLOBVector {
        val vector = DefBLOBVector()
        vector.parseDefVector()
        vector.parseVectorElements(::parseDefBLOB)
        return vector
    }

    private fun NewVector<*>.parseNewVector() {
        device = reader.attribute(DEVICE_ATTR_NAME)!!
        name = reader.attribute(NAME_ATTR_NAME)!!
        timestamp = reader.attribute(TIMESTAMP_ATTR_NAME) ?: ""
    }

    private fun parseNewSwitchVector(): NewSwitchVector {
        val vector = NewSwitchVector()
        vector.parseNewVector()
        vector.parseVectorElements(::parseOneSwitch)
        return vector
    }

    private fun parseNewNumberVector(): NewNumberVector {
        val vector = NewNumberVector()
        vector.parseNewVector()
        vector.parseVectorElements(::parseOneNumber)
        return vector
    }

    private fun parseNewTextVector(): NewTextVector {
        val vector = NewTextVector()
        vector.parseNewVector()
        vector.parseVectorElements(::parseOneText)
        return vector
    }

    private fun parseNewLightVector(): NewLightVector {
        val vector = NewLightVector()
        vector.parseNewVector()
        vector.parseVectorElements(::parseOneLight)
        return vector
    }

    private fun parseNewBLOBVector(): NewBLOBVector {
        val vector = NewBLOBVector()
        vector.parseNewVector()
        vector.parseVectorElements(::parseOneBLOB)
        return vector
    }

    private fun SetVector<*>.parseSetVector() {
        device = reader.attribute(DEVICE_ATTR_NAME)!!
        name = reader.attribute(NAME_ATTR_NAME)!!
        state = PropertyState.parse(reader.attribute(STATE_ATTR_NAME)!!)
        reader.attribute(TIMEOUT_ATTR_NAME)?.toDoubleOrNull()?.also { timeout = it }
        timestamp = reader.attribute(TIMESTAMP_ATTR_NAME) ?: ""
        reader.attribute(MESSAGE_ATTR_NAME)?.also { message = it }
    }

    private fun parseSetSwitchVector(): SetSwitchVector {
        val vector = SetSwitchVector()
        vector.parseSetVector()
        vector.parseVectorElements(::parseOneSwitch)
        return vector
    }

    private fun parseSetNumberVector(): SetNumberVector {
        val vector = SetNumberVector()
        vector.parseSetVector()
        vector.parseVectorElements(::parseOneNumber)
        return vector
    }

    private fun parseSetTextVector(): SetTextVector {
        val vector = SetTextVector()
        vector.parseSetVector()
        vector.parseVectorElements(::parseOneText)
        return vector
    }

    private fun parseSetLightVector(): SetLightVector {
        val vector = SetLightVector()
        vector.parseSetVector()
        vector.parseVectorElements(::parseOneLight)
        return vector
    }

    private fun parseSetBLOBVector(): SetBLOBVector {
        val vector = SetBLOBVector()
        vector.parseSetVector()
        vector.parseVectorElements(::parseOneBLOB)
        return vector
    }

    // ELEMENT

    private fun <E : Element<*>> Vector<E>.parseVectorElements(action: () -> E) {
        val name = reader.localName

        while (reader.hasNext()) {
            val type = reader.next()

            if (type == XMLStreamConstants.END_ELEMENT && reader.localName == name) {
                break
            } else if (type == XMLStreamConstants.START_ELEMENT) {
                elements.add(action())
            }
        }
    }

    private fun DefElement<*>.parseDefElement() {
        name = reader.attribute(NAME_ATTR_NAME)!!
        label = reader.attribute(LABEL_ATTR_NAME)!!
    }

    private fun parseDefSwitch(): DefSwitch {
        val defSwitch = DefSwitch()
        defSwitch.parseDefElement()
        defSwitch.value = reader.elementText.trim().equals("On", true)
        return defSwitch
    }

    private fun parseDefText(): DefText {
        val defText = DefText()
        defText.parseDefElement()
        defText.value = reader.elementText.trim()
        return defText
    }

    private fun parseDefLight(): DefLight {
        val defLight = DefLight()
        defLight.parseDefElement()
        defLight.value = PropertyState.parse(reader.elementText.trim())
        return defLight
    }

    private fun parseDefBLOB(): DefBLOB {
        val defBLOB = DefBLOB()
        defBLOB.parseDefElement()
        return defBLOB
    }

    private fun parseDefNumber(): DefNumber {
        val defNumber = DefNumber()
        defNumber.parseDefElement()
        defNumber.max = reader.attribute(MAX_ATTR_NAME)!!.toDouble()
        defNumber.min = reader.attribute(MIN_ATTR_NAME)!!.toDouble()
        defNumber.step = reader.attribute(STEP_ATTR_NAME)!!.toDouble()
        defNumber.format = reader.attribute(FORMAT_ATTR_NAME) ?: ""
        defNumber.value = reader.elementText.trim().toDouble()
        return defNumber
    }

    private fun OneElement<*>.parseOneElement() {
        name = reader.attribute(NAME_ATTR_NAME)!!
    }

    private fun parseOneSwitch() = OneSwitch().apply {
        parseOneElement()
        value = reader.elementText.trim().equals("On", true)
    }

    private fun parseOneText() = OneText().apply {
        parseOneElement()
        value = reader.elementText.trim()
    }

    private fun parseOneLight() = OneLight().apply {
        parseOneElement()
        value = PropertyState.parse(reader.elementText.trim())
    }

    private fun parseOneBLOB() = OneBLOB().apply {
        parseOneElement()
        size = reader.attribute(SIZE_ATTR_NAME) ?: ""
        format = reader.attribute(FORMAT_ATTR_NAME) ?: ""
        value = reader.elementText
    }

    private fun parseOneNumber() = OneNumber().apply {
        parseOneElement()
        value = reader.elementText.trim().toDouble()
    }

    private fun parseGetProperties() = GetProperties().apply {
        device = reader.attribute(DEVICE_ATTR_NAME)!!
        name = reader.attribute(NAME_ATTR_NAME)!!
    }

    private fun parseDelProperty() = DelProperty().apply {
        device = reader.attribute(DEVICE_ATTR_NAME)!!
        name = reader.attribute(NAME_ATTR_NAME) ?: ""
        timestamp = reader.attribute(TIMESTAMP_ATTR_NAME) ?: ""
        reader.attribute(MESSAGE_ATTR_NAME)?.also { message = it }
    }

    private fun parseMessage() = Message().apply {
        device = reader.attribute(DEVICE_ATTR_NAME)!!
        timestamp = reader.attribute(TIMESTAMP_ATTR_NAME) ?: ""
        message = reader.attribute(MESSAGE_ATTR_NAME)!!
    }

    private fun parseEnableBLOB() = EnableBLOB().apply {
        device = reader.attribute(DEVICE_ATTR_NAME)!!
        name = reader.attribute(NAME_ATTR_NAME)!!
        value = BLOBEnable.parse(reader.elementText.trim())
    }

    override fun close() {
        reader.close()
    }

    companion object {

        @JvmStatic private val XML_INPUT_FACTORY = InputFactoryImpl()

        private const val DEF_SWITCH_VECTOR_NAME = "defSwitchVector"
        private const val DEF_NUMBER_VECTOR_NAME = "defNumberVector"
        private const val DEF_TEXT_VECTOR_NAME = "defTextVector"
        private const val DEF_LIGHT_VECTOR_NAME = "defLightVector"
        private const val DEF_BLOB_VECTOR_NAME = "defBLOBVector"
        private const val NEW_SWITCH_VECTOR_NAME = "newSwitchVector"
        private const val NEW_NUMBER_VECTOR_NAME = "newNumberVector"
        private const val NEW_TEXT_VECTOR_NAME = "newTextVector"
        private const val NEW_LIGHT_VECTOR_NAME = "newLightVector"
        private const val NEW_BLOB_VECTOR_NAME = "newBLOBVector"
        private const val SET_SWITCH_VECTOR_NAME = "setSwitchVector"
        private const val SET_NUMBER_VECTOR_NAME = "setNumberVector"
        private const val SET_TEXT_VECTOR_NAME = "setTextVector"
        private const val SET_LIGHT_VECTOR_NAME = "setLightVector"
        private const val SET_BLOB_VECTOR_NAME = "setBLOBVector"
        private const val GET_PROPERTIES_NAME = "getProperties"
        private const val DEL_PROPERTY_NAME = "delProperty"
        private const val MESSAGE_NAME = "message"
        private const val ENABLE_BLOB_NAME = "enableBLOB"
        private const val DEVICE_ATTR_NAME = "device"
        private const val NAME_ATTR_NAME = "name"
        private const val LABEL_ATTR_NAME = "label"
        private const val GROUP_ATTR_NAME = "group"
        private const val STATE_ATTR_NAME = "state"
        private const val RULE_ATTR_NAME = "rule"
        private const val PERM_ATTR_NAME = "perm"
        private const val TIMEOUT_ATTR_NAME = "timeout"
        private const val TIMESTAMP_ATTR_NAME = "timestamp"
        private const val SIZE_ATTR_NAME = "size"
        private const val FORMAT_ATTR_NAME = "format"
        private const val MIN_ATTR_NAME = "min"
        private const val MAX_ATTR_NAME = "max"
        private const val STEP_ATTR_NAME = "step"
        private const val MESSAGE_ATTR_NAME = "message"
    }
}
