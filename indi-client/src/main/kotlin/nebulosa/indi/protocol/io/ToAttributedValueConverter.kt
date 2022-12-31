package nebulosa.indi.protocol.io

import com.thoughtworks.xstream.converters.*
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider
import com.thoughtworks.xstream.core.util.FastField
import com.thoughtworks.xstream.core.util.HierarchicalStreams
import com.thoughtworks.xstream.core.util.Primitives
import com.thoughtworks.xstream.io.HierarchicalStreamReader
import com.thoughtworks.xstream.io.HierarchicalStreamWriter
import com.thoughtworks.xstream.mapper.Mapper
import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal class ToAttributedValueConverter @JvmOverloads constructor(
    private val type: Class<*>,
    private val mapper: Mapper,
    private val reflectionProvider: ReflectionProvider,
    private val lookup: ConverterLookup,
    valueFieldName: String? = null,
    valueDefinedIn: Class<*>? = null,
) : Converter {

    private var valueField: Field? = null

    init {
        if (valueFieldName == null) {
            valueField = null
        } else {
            try {
                val field = (valueDefinedIn ?: type).getDeclaredField(valueFieldName)
                field.isAccessible = true
                valueField = field
            } catch (e: NoSuchFieldException) {
                throw IllegalArgumentException(e.message + ": " + valueFieldName)
            }
        }
    }

    override fun canConvert(type: Class<*>) = this.type == type

    override fun marshal(
        source: Any,
        writer: HierarchicalStreamWriter,
        context: MarshallingContext,
    ) {
        val sourceType = source.javaClass
        val defaultFieldDefinition = HashMap<Any?, Any?>()
        val tagValue = arrayOfNulls<String>(1)
        val realValue = arrayOfNulls<Any>(1)
        val fieldType = arrayOfNulls<Class<*>>(1)
        val definingType = arrayOfNulls<Class<*>>(1)

        reflectionProvider.visitSerializableFields(
            source,
            ReflectionProvider.Visitor { fieldName, type, definedIn, value ->
                if (!mapper.shouldSerializeMember(definedIn, fieldName)) {
                    return@Visitor
                }

                val field = FastField(definedIn, fieldName)
                val alias = mapper.serializedMember(definedIn, fieldName)

                if (!defaultFieldDefinition.containsKey(alias)) {
                    defaultFieldDefinition[alias] = reflectionProvider.getField(sourceType, fieldName)
                } else if (!fieldIsEqual(field)) {
                    val exception = ConversionException("Cannot write attribute twice for object")
                    exception.add("alias", alias)
                    exception.add("type", sourceType.name)
                    throw exception
                }

                var converter = mapper.getLocalConverter(definedIn, fieldName) as? ConverterMatcher

                if (converter == null) {
                    converter = lookup.lookupConverterForType(type)
                }

                if (value != null) {
                    val isValueField = valueField != null && fieldIsEqual(field)
                    if (isValueField) {
                        definingType[0] = definedIn
                        fieldType[0] = type
                        realValue[0] = value
                        tagValue[0] = STRUCTURE_MARKER
                    }

                    if (converter is SingleValueConverter) {
                        val str = converter.toString(value)

                        if (isValueField) {
                            tagValue[0] = str
                        } else {
                            if (str != null) {
                                writer.addAttribute(alias, str)
                            }
                        }
                    } else {
                        if (!isValueField) {
                            val exception = ConversionException("Cannot write element as attribute")
                            exception.add("alias", alias)
                            exception.add("type", sourceType.name)
                            throw exception
                        }
                    }
                }
            })

        if (tagValue[0] != null) {
            val actualType = realValue[0]!!.javaClass
            val defaultType = mapper.defaultImplementationOf(fieldType[0])

            if (actualType != defaultType) {
                val serializedClassName = mapper.serializedClass(actualType)
                if (serializedClassName != mapper.serializedClass(defaultType)) {
                    val attributeName = mapper.aliasForSystemAttribute("class")

                    if (attributeName != null) {
                        writer.addAttribute(attributeName, serializedClassName)
                    }
                }
            }

            if (tagValue[0] === STRUCTURE_MARKER) {
                context.convertAnother(realValue[0])
            } else {
                writer.setValue(tagValue[0])
            }
        }
    }

    // TODO: Can be optimized?
    override fun unmarshal(
        reader: HierarchicalStreamReader,
        context: UnmarshallingContext,
    ): Any {
        val result = reflectionProvider.newInstance(context.requiredType)
        val resultType = result.javaClass
        val seenFields = HashSet<FastField>()
        val it = reader.attributeNames

        val systemAttributes = HashSet<Any?>()
        systemAttributes.add(mapper.aliasForSystemAttribute("class"))

        // Process attributes before recursing into child elements.
        while (it.hasNext()) {
            val attrName = it.next() as String

            if (systemAttributes.contains(attrName)) {
                continue
            }

            val fieldName = mapper.realMember(resultType, attrName)
            val field = reflectionProvider.getFieldOrNull(resultType, fieldName)

            if (field != null) {
                if (Modifier.isTransient(field.modifiers)) {
                    continue
                }

                var type = field.type
                val declaringClass = field.declaringClass
                var converter = mapper.getLocalConverter(declaringClass, fieldName) as? ConverterMatcher

                if (converter == null) {
                    converter = lookup.lookupConverterForType(type)
                }

                if (converter !is SingleValueConverter) {
                    val exception = ConversionException("Cannot read field as a single value for object")
                    exception.add("field", fieldName)
                    exception.add("type", resultType.name)
                    throw exception
                }

                val value = converter.fromString(reader.getAttribute(attrName))

                if (type.isPrimitive) {
                    type = Primitives.box(type)
                }

                if (value != null && !type.isAssignableFrom(value.javaClass)) {
                    val exception = ConversionException("Cannot assign object to type")
                    exception.add("object type", value.javaClass.name)
                    exception.add("target type", type.name)
                    throw exception
                }

                reflectionProvider.writeField(result, fieldName, value, declaringClass)

                if (!seenFields.add(FastField(declaringClass, fieldName))) {
                    throw AbstractReflectionConverter.DuplicateFieldException(
                        "$fieldName [${declaringClass.name}]"
                    )
                }
            }
        }

        if (valueField != null) {
            val classDefiningField = valueField!!.declaringClass
            val fieldName = valueField!!.name
            val field = reflectionProvider.getField(classDefiningField, fieldName)

            if (field == null) {
                val exception = ConversionException("Cannot assign value to field of type")
                exception.add("element", reader.nodeName)
                exception.add("field", fieldName)
                exception.add("target type", context.requiredType.name)
                throw exception
            }

            val classAttribute = HierarchicalStreams.readClassAttribute(reader, mapper)

            var type = if (classAttribute != null) {
                mapper.realClass(classAttribute)
            } else {
                mapper.defaultImplementationOf(reflectionProvider.getFieldType(result, fieldName, classDefiningField))
            }

            val value = context.convertAnother(
                result, type,
                mapper.getLocalConverter(field.declaringClass, field.name)
            )

            val definedType = reflectionProvider.getFieldType(result, fieldName, classDefiningField)

            if (!definedType.isPrimitive) {
                type = definedType
            }

            if (value != null && !type.isAssignableFrom(value.javaClass)) {
                val exception = ConversionException("Cannot assign object to type")
                exception.add("object type", value.javaClass.name)
                exception.add("target type", type.name)
                throw exception
            }

            reflectionProvider.writeField(result, fieldName, value, classDefiningField)

            if (!seenFields.add(FastField(classDefiningField, fieldName))) {
                throw AbstractReflectionConverter.DuplicateFieldException(
                    "$fieldName [${classDefiningField.name}]"
                )
            }
        }

        return result
    }

    private fun fieldIsEqual(field: FastField): Boolean {
        return valueField!!.name == field.name &&
                valueField!!.declaringClass.name == field.declaringClass
    }

    companion object {

        private const val STRUCTURE_MARKER = ""
    }
}
