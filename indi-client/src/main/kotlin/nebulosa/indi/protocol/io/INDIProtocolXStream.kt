package nebulosa.indi.protocol.io

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.converters.SingleValueConverter
import com.thoughtworks.xstream.converters.SingleValueConverterWrapper
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider
import com.thoughtworks.xstream.io.HierarchicalStreamDriver
import com.thoughtworks.xstream.io.xml.StaxDriver
import nebulosa.indi.protocol.*
import org.slf4j.LoggerFactory

internal class INDIProtocolXStream(val driver: HierarchicalStreamDriver = StaxDriver()) :
    XStream(PureJavaReflectionProvider(), driver) {

    init {
        processAnnotations(DefBLOB::class.java)
        processAnnotations(DefBLOBVector::class.java)
        processAnnotations(DefLight::class.java)
        processAnnotations(DefLightVector::class.java)
        processAnnotations(DefNumber::class.java)
        processAnnotations(DefNumberVector::class.java)
        processAnnotations(DefSwitch::class.java)
        processAnnotations(DefSwitchVector::class.java)
        processAnnotations(DefText::class.java)
        processAnnotations(DefTextVector::class.java)
        processAnnotations(DelProperty::class.java)
        processAnnotations(EnableBLOB::class.java)
        processAnnotations(GetProperties::class.java)
        processAnnotations(INDIProtocol::class.java)
        processAnnotations(Message::class.java)
        processAnnotations(NewBLOBVector::class.java)
        processAnnotations(NewLightVector::class.java)
        processAnnotations(NewNumberVector::class.java)
        processAnnotations(NewSwitchVector::class.java)
        processAnnotations(NewTextVector::class.java)
        processAnnotations(OneBLOB::class.java)
        processAnnotations(OneLight::class.java)
        processAnnotations(OneNumber::class.java)
        processAnnotations(OneSwitch::class.java)
        processAnnotations(OneText::class.java)
        processAnnotations(SetBLOBVector::class.java)
        processAnnotations(SetLightVector::class.java)
        processAnnotations(SetNumberVector::class.java)
        processAnnotations(SetSwitchVector::class.java)
        processAnnotations(SetTextVector::class.java)

        allowTypesByWildcard(arrayOf("nebulosa.indi.protocol.**"))

        registerConverter(hasTextEnumConverter<SwitchRule>())
        registerConverter(hasTextEnumConverter<SwitchState>())
        registerConverter(hasTextEnumConverter<PropertyPermission>())
        registerConverter(hasTextEnumConverter<PropertyState>())
        registerConverter(hasTextEnumConverter<BLOBEnable>())
    }

    override fun registerConverter(converter: SingleValueConverter, priority: Int) {
        super.registerConverter(object : SingleValueConverterWrapper(converter) {

            override fun fromString(str: String?) = try {
                converter.fromString(str?.trim())
            } catch (e: Throwable) {
                LOG.error("protocol conversion error", e)
                null
            }

            override fun toString(obj: Any?) = try {
                converter.toString(if (obj == "") null else obj)
            } catch (e: Throwable) {
                LOG.error("protocol conversion error", e)
                null
            }
        } as SingleValueConverter, priority)
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(INDIProtocolXStream::class.java)

        internal inline fun <reified T> hasTextEnumConverter(): HasTextEnumConverter<T> where T : Enum<T>, T : HasText {
            return HasTextEnumConverter(T::class.java)
        }
    }
}
