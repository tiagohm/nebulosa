package nebulosa.api.beans.processors

import nebulosa.api.beans.annotations.Subscriber
import org.greenrobot.eventbus.EventBus
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor
import org.springframework.stereotype.Component

@Component
class EventBusBeanPostProcessor(private val eventBus: EventBus) : DestructionAwareBeanPostProcessor {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean::class.java.isAnnotationPresent(Subscriber::class.java)) {
            eventBus.register(bean)
        }

        return bean
    }

    override fun postProcessBeforeDestruction(bean: Any, beanName: String) {
        if (bean::class.java.isAnnotationPresent(Subscriber::class.java)) {
            eventBus.unregister(bean)
        }
    }
}
