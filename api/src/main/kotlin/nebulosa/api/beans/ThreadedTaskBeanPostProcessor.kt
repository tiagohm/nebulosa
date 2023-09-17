package nebulosa.api.beans

import nebulosa.api.beans.annotations.ThreadedTask
import nebulosa.log.loggerFor
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class ThreadedTaskBeanPostProcessor : BeanPostProcessor {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean is Runnable && bean::class.java.isAnnotationPresent(ThreadedTask::class.java)) {
            LOG.info("threaded task scheduled. name={}", beanName)

            CompletableFuture
                .runAsync(bean)
                .whenComplete { _, e -> e?.printStackTrace() ?: LOG.info("threaded task finished. name={}", beanName) }
        }

        return bean
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<ThreadedTaskBeanPostProcessor>()
    }
}
