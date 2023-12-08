package nebulosa.api.beans.configurations

import nebulosa.common.concurrency.DaemonThreadFactory
import nebulosa.log.loggerFor
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.batch.BatchDataSourceScriptDatabaseInitializer
import org.springframework.boot.autoconfigure.batch.BatchProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class BatchConfiguration(
    @Qualifier("batchDataSource") private val batchDataSource: DataSource,
    @Qualifier("batchTransactionManager") private val batchTransactionManager: PlatformTransactionManager,
) : DefaultBatchConfiguration() {

    override fun getDataSource(): DataSource {
        return batchDataSource
    }

    override fun getTransactionManager(): PlatformTransactionManager {
        return batchTransactionManager
    }

    override fun getTaskExecutor(): TaskExecutor {
        return SimpleAsyncTaskExecutor(DaemonThreadFactory)
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.batch")
    fun batchProperties(): BatchProperties {
        return BatchProperties()
    }

    @Bean
    fun batchDataSourceScriptDatabaseInitializer(batchProperties: BatchProperties): BatchDataSourceScriptDatabaseInitializer {
        val initializer = BatchDataSourceScriptDatabaseInitializer(batchDataSource, batchProperties.jdbc)
        LOG.info("batch database initialized: {}", initializer.initializeDatabase())
        return initializer
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<BatchConfiguration>()
    }
}
