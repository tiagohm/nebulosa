package nebulosa.api.beans.annotations

import org.springframework.context.annotation.Lazy

@Retention
@Lazy(false)
@Target(AnnotationTarget.CLASS)
annotation class Subscriber
