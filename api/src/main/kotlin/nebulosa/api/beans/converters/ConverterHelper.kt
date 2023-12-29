package nebulosa.api.beans.converters

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.servlet.HandlerMapping

@Suppress("UNCHECKED_CAST")
val NativeWebRequest.pathVariables
    get() = getNativeRequest(HttpServletRequest::class.java)!!
        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as Map<String, String>

fun NativeWebRequest.parameter(name: String) =
    pathVariables[name]?.ifBlank { null }
        ?: getParameter(name)?.ifBlank { null }
        ?: getNativeRequest(HttpServletRequest::class.java)!!.getParameter(name)?.ifBlank { null }

inline fun <reified T : Annotation> MethodParameter.hasAnnotation(): Boolean {
    return hasParameterAnnotation(T::class.java)
}

inline fun <reified T : Annotation> MethodParameter.annotation(): T? {
    return getParameterAnnotation(T::class.java)
}
