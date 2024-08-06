package nebulosa.api.beans.annotations

import org.springframework.context.annotation.Lazy

@Lazy(false)
@Target(AnnotationTarget.CLASS)
annotation class Subscriber
