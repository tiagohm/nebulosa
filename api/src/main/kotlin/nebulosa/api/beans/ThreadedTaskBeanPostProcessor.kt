package nebulosa.api.beans

import nebulosa.api.beans.annotations.ThreadedTask
import nebulosa.log.loggerFor
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

@Component
class ThreadedTaskBeanPostProcessor(private val systemExecutorService: ExecutorService) : BeanPostProcessor {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean is Runnable && bean::class.java.isAnnotationPresent(ThreadedTask::class.java)) {
            LOG.info("loader scheduled. name={}", beanName)

            CompletableFuture
                .runAsync(bean, systemExecutorService)
                .whenComplete { _, e -> e?.printStackTrace() }
        }

        return bean
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<ThreadedTaskBeanPostProcessor>()
    }
}
