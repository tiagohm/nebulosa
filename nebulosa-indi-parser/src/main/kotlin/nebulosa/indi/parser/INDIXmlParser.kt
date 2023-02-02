package nebulosa.indi.parser

import nebulosa.indi.connection.io.INDIInputStream
import nebulosa.indi.protocol.*
import java.io.InputStream
import javax.xml.namespace.QName
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement

open class INDIXmlParser(private val source: InputStream) : INDIInputStream {

    private val reader = XML_INPUT_FACTORY.createXMLEventReader(source)

    @Synchronized
    override fun readINDIProtocol(): INDIProtocol? {
        while (reader.hasNext()) {
            val event = reader.nextEvent()

            println(event)

            if (event is StartElement) {
                val message = parse(reader, event)
                if (message != null) return message
            }
        }

        println("exit")

        return null
    }

    private fun parse(reader: XMLEventReader, element: StartElement): INDIProtocol? {
        return when (element.name) {
            SET_SWITCH_VECTOR_NAME -> parseSetSwitchVector(reader, element)
            SET_NUMBER_VECTOR_NAME -> parseSetNumberVector(reader, element)
            SET_TEXT_VECTOR_NAME -> parseSetTextVector(reader, element)
            SET_LIGHT_VECTOR_NAME -> parseSetLightVector(reader, element)
            SET_BLOB_VECTOR_NAME -> parseSetBLOBVector(reader, element)
            DEF_SWITCH_VECTOR_NAME -> parseDefSwitchVector(reader, element)
            DEF_NUMBER_VECTOR_NAME -> parseDefNumberVector(reader, element)
            DEF_TEXT_VECTOR_NAME -> parseDefTextVector(reader, element)
            DEF_LIGHT_VECTOR_NAME -> parseDefLightVector(reader, element)
            DEF_BLOB_VECTOR_NAME -> parseDefBLOBVector(reader, element)
            NEW_SWITCH_VECTOR_NAME -> parseNewSwitchVector(reader, element)
            NEW_NUMBER_VECTOR_NAME -> parseNewNumberVector(reader, element)
            NEW_TEXT_VECTOR_NAME -> parseNewTextVector(reader, element)
            NEW_LIGHT_VECTOR_NAME -> parseNewLightVector(reader, element)
            NEW_BLOB_VECTOR_NAME -> parseNewBLOBVector(reader, element)
            else -> null
        }
    }

    // VECTOR.

    private fun DefVector<*>.parseDefVector(element: StartElement) {
        device = element.getAttributeByName(DEVICE_ATTR_NAME).value
        name = element.getAttributeByName(NAME_ATTR_NAME).value
        label = element.getAttributeByName(LABEL_ATTR_NAME).value
        group = element.getAttributeByName(GROUP_ATTR_NAME).value
        state = PropertyState.parse(element.getAttributeByName(STATE_ATTR_NAME).value)
        element.getAttributeByName(PERM_ATTR_NAME)?.value?.also { perm = PropertyPermission.parse(it) }
        element.getAttributeByName(TIMEOUT_ATTR_NAME)?.value?.toDoubleOrNull()?.also { timeout = it }
        timestamp = element.getAttributeByName(TIMESTAMP_ATTR_NAME).value
    }

    private fun parseDefSwitchVector(reader: XMLEventReader, element: StartElement): DefSwitchVector {
        val vector = DefSwitchVector()
        vector.parseDefVector(element)
        vector.rule = SwitchRule.parse(element.getAttributeByName(RULE_ATTR_NAME).value)
        vector.parseVectorElements(reader, element, ::parseDefSwitch)
        return vector
    }

    private fun parseDefNumberVector(reader: XMLEventReader, element: StartElement): DefNumberVector {
        val vector = DefNumberVector()
        vector.parseDefVector(element)
        vector.parseVectorElements(reader, element, ::parseDefNumber)
        return vector
    }

    private fun parseDefTextVector(reader: XMLEventReader, element: StartElement): DefTextVector {
        val vector = DefTextVector()
        vector.parseDefVector(element)
        vector.parseVectorElements(reader, element, ::parseDefText)
        return vector
    }

    private fun parseDefLightVector(reader: XMLEventReader, element: StartElement): DefLightVector {
        val vector = DefLightVector()
        vector.parseDefVector(element)
        vector.parseVectorElements(reader, element, ::parseDefLight)
        return vector
    }

    private fun parseDefBLOBVector(reader: XMLEventReader, element: StartElement): DefBLOBVector {
        val vector = DefBLOBVector()
        vector.parseDefVector(element)
        vector.parseVectorElements(reader, element, ::parseDefBLOB)
        return vector
    }

    private fun NewVector<*>.parseNewVector(element: StartElement) {
        device = element.getAttributeByName(DEVICE_ATTR_NAME).value
        name = element.getAttributeByName(NAME_ATTR_NAME).value
        timestamp = element.getAttributeByName(TIMESTAMP_ATTR_NAME).value
    }

    private fun parseNewSwitchVector(reader: XMLEventReader, element: StartElement): NewSwitchVector {
        val vector = NewSwitchVector()
        vector.parseNewVector(element)
        vector.parseVectorElements(reader, element, ::parseOneSwitch)
        return vector
    }

    private fun parseNewNumberVector(reader: XMLEventReader, element: StartElement): NewNumberVector {
        val vector = NewNumberVector()
        vector.parseNewVector(element)
        vector.parseVectorElements(reader, element, ::parseOneNumber)
        return vector
    }

    private fun parseNewTextVector(reader: XMLEventReader, element: StartElement): NewTextVector {
        val vector = NewTextVector()
        vector.parseNewVector(element)
        vector.parseVectorElements(reader, element, ::parseOneText)
        return vector
    }

    private fun parseNewLightVector(reader: XMLEventReader, element: StartElement): NewLightVector {
        val vector = NewLightVector()
        vector.parseNewVector(element)
        vector.parseVectorElements(reader, element, ::parseOneLight)
        return vector
    }

    private fun parseNewBLOBVector(reader: XMLEventReader, element: StartElement): NewBLOBVector {
        val vector = NewBLOBVector()
        vector.parseNewVector(element)
        vector.parseVectorElements(reader, element, ::parseOneBLOB)
        return vector
    }

    private fun SetVector<*>.parseSetVector(element: StartElement) {
        device = element.getAttributeByName(DEVICE_ATTR_NAME).value
        name = element.getAttributeByName(NAME_ATTR_NAME).value
        state = PropertyState.parse(element.getAttributeByName(STATE_ATTR_NAME).value)
        element.getAttributeByName(TIMEOUT_ATTR_NAME)?.value?.toDoubleOrNull()?.also { timeout = it }
        timestamp = element.getAttributeByName(TIMESTAMP_ATTR_NAME).value
        message = element.getAttributeByName(MESSAGE_ATTR_NAME).value
    }

    private fun parseSetSwitchVector(reader: XMLEventReader, element: StartElement): SetSwitchVector {
        val vector = SetSwitchVector()
        vector.parseSetVector(element)
        vector.parseVectorElements(reader, element, ::parseOneSwitch)
        return vector
    }

    private fun parseSetNumberVector(reader: XMLEventReader, element: StartElement): SetNumberVector {
        val vector = SetNumberVector()
        vector.parseSetVector(element)
        vector.parseVectorElements(reader, element, ::parseOneNumber)
        return vector
    }

    private fun parseSetTextVector(reader: XMLEventReader, element: StartElement): SetTextVector {
        val vector = SetTextVector()
        vector.parseSetVector(element)
        vector.parseVectorElements(reader, element, ::parseOneText)
        return vector
    }

    private fun parseSetLightVector(reader: XMLEventReader, element: StartElement): SetLightVector {
        val vector = SetLightVector()
        vector.parseSetVector(element)
        vector.parseVectorElements(reader, element, ::parseOneLight)
        return vector
    }

    private fun parseSetBLOBVector(reader: XMLEventReader, element: StartElement): SetBLOBVector {
        val vector = SetBLOBVector()
        vector.parseSetVector(element)
        vector.parseVectorElements(reader, element, ::parseOneBLOB)
        return vector
    }

    // ELEMENT

    private fun <E : Element<*>> Vector<E>.parseVectorElements(
        reader: XMLEventReader, parent: StartElement,
        action: (XMLEventReader, StartElement) -> E,
    ) {
        while (reader.hasNext()) {
            val event = reader.nextEvent()

            if (event is EndElement && event.name == parent.name) {
                break
            } else if (event is StartElement) {
                elements.add(action(reader, event))
            }
        }
    }

    private fun DefElement<*>.parseDefElement(element: StartElement) {
        name = element.getAttributeByName(NAME_ATTR_NAME).value
        label = element.getAttributeByName(LABEL_ATTR_NAME).value
    }

    private fun parseDefSwitch(reader: XMLEventReader, element: StartElement): DefSwitch {
        val number = DefSwitch()
        number.parseDefElement(element)
        number.value = reader.elementText.trim().equals("On", true)
        return number
    }

    private fun parseDefText(reader: XMLEventReader, element: StartElement): DefText {
        val number = DefText()
        number.parseDefElement(element)
        number.value = reader.elementText.trim()
        return number
    }

    private fun parseDefLight(reader: XMLEventReader, element: StartElement): DefLight {
        val number = DefLight()
        number.parseDefElement(element)
        number.value = PropertyState.parse(reader.elementText.trim())
        return number
    }

    private fun parseDefBLOB(reader: XMLEventReader, element: StartElement): DefBLOB {
        val number = DefBLOB()
        number.parseDefElement(element)
        return number
    }

    private fun parseDefNumber(reader: XMLEventReader, element: StartElement): DefNumber {
        val number = DefNumber()

        number.parseDefElement(element)
        number.max = element.getAttributeByName(MAX_ATTR_NAME).value.toDouble()
        number.min = element.getAttributeByName(MIN_ATTR_NAME).value.toDouble()
        number.step = element.getAttributeByName(STEP_ATTR_NAME).value.toDouble()
        number.format = element.getAttributeByName(FORMAT_ATTR_NAME).value
        number.value = reader.elementText.trim().toDouble()

        return number
    }

    private fun OneElement<*>.parseOneElement(element: StartElement) {
        name = element.getAttributeByName(NAME_ATTR_NAME).value
    }

    private fun parseOneSwitch(reader: XMLEventReader, element: StartElement): OneSwitch {
        val number = OneSwitch()
        number.parseOneElement(element)
        number.value = reader.elementText.trim().equals("On", true)
        return number
    }

    private fun parseOneText(reader: XMLEventReader, element: StartElement): OneText {
        val number = OneText()
        number.parseOneElement(element)
        number.value = reader.elementText.trim()
        return number
    }

    private fun parseOneLight(reader: XMLEventReader, element: StartElement): OneLight {
        val number = OneLight()
        number.parseOneElement(element)
        number.value = PropertyState.parse(reader.elementText.trim())
        return number
    }

    private fun parseOneBLOB(reader: XMLEventReader, element: StartElement): OneBLOB {
        val number = OneBLOB()
        number.parseOneElement(element)
        number.value = reader.elementText
        return number
    }

    private fun parseOneNumber(reader: XMLEventReader, element: StartElement): OneNumber {
        val number = OneNumber()
        number.parseOneElement(element)
        number.value = reader.elementText.trim().toDouble()
        return number
    }

    override fun close() = source.close()

    companion object {

        @JvmStatic private val XML_INPUT_FACTORY = XMLInputFactory.newInstance()

        @JvmStatic private val DEF_SWITCH_VECTOR_NAME = QName("defSwitchVector")
        @JvmStatic private val DEF_NUMBER_VECTOR_NAME = QName("defNumberVector")
        @JvmStatic private val DEF_TEXT_VECTOR_NAME = QName("defTextVector")
        @JvmStatic private val DEF_LIGHT_VECTOR_NAME = QName("defLightVector")
        @JvmStatic private val DEF_BLOB_VECTOR_NAME = QName("defBLOBVector")
        @JvmStatic private val NEW_SWITCH_VECTOR_NAME = QName("newSwitchVector")
        @JvmStatic private val NEW_NUMBER_VECTOR_NAME = QName("newNumberVector")
        @JvmStatic private val NEW_TEXT_VECTOR_NAME = QName("newTextVector")
        @JvmStatic private val NEW_LIGHT_VECTOR_NAME = QName("newLightVector")
        @JvmStatic private val NEW_BLOB_VECTOR_NAME = QName("newBLOBVector")
        @JvmStatic private val SET_SWITCH_VECTOR_NAME = QName("setSwitchVector")
        @JvmStatic private val SET_NUMBER_VECTOR_NAME = QName("setNumberVector")
        @JvmStatic private val SET_TEXT_VECTOR_NAME = QName("setTextVector")
        @JvmStatic private val SET_LIGHT_VECTOR_NAME = QName("setLightVector")
        @JvmStatic private val SET_BLOB_VECTOR_NAME = QName("setBLOBVector")
        @JvmStatic private val DEVICE_ATTR_NAME = QName("device")
        @JvmStatic private val NAME_ATTR_NAME = QName("name")
        @JvmStatic private val LABEL_ATTR_NAME = QName("label")
        @JvmStatic private val GROUP_ATTR_NAME = QName("group")
        @JvmStatic private val STATE_ATTR_NAME = QName("state")
        @JvmStatic private val RULE_ATTR_NAME = QName("rule")
        @JvmStatic private val PERM_ATTR_NAME = QName("perm")
        @JvmStatic private val TIMEOUT_ATTR_NAME = QName("timeout")
        @JvmStatic private val TIMESTAMP_ATTR_NAME = QName("timestamp")
        @JvmStatic private val FORMAT_ATTR_NAME = QName("format")
        @JvmStatic private val MIN_ATTR_NAME = QName("min")
        @JvmStatic private val MAX_ATTR_NAME = QName("max")
        @JvmStatic private val STEP_ATTR_NAME = QName("step")
        @JvmStatic private val MESSAGE_ATTR_NAME = QName("message")
    }
}
