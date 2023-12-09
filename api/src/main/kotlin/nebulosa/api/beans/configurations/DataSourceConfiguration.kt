package nebulosa.api.beans.configurations

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class DataSourceConfiguration {

    @Value("\${spring.datasource.url}") private lateinit var mainDataSourceUrl: String
    @Value("\${spring.batch.datasource.url}") private lateinit var batchDataSourceUrl: String

    @Primary
    @Bean("mainDataSource")
    fun mainDataSource(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = mainDataSourceUrl
        config.driverClassName = DRIVER_CLASS_NAME
        config.maximumPoolSize = 1
        config.minimumIdle = 1
        return HikariDataSource(config)
    }

    @Bean("batchDataSource")
    fun batchDataSource(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = batchDataSourceUrl
        config.driverClassName = DRIVER_CLASS_NAME
        config.maximumPoolSize = 1
        config.minimumIdle = 1
        return HikariDataSource(config)
    }

    @Configuration
    @EnableJpaRepositories(
        basePackages = ["nebulosa.api"],
        entityManagerFactoryRef = "mainEntityManagerFactory",
        transactionManagerRef = "mainTransactionManager"
    )
    class Main {

        @Primary
        @Bean("mainEntityManagerFactory")
        fun mainEntityManagerFactory(
            builder: EntityManagerFactoryBuilder,
            dataSource: DataSource,
        ) = builder
            .dataSource(dataSource)
            .packages("nebulosa.api")
            .persistenceUnit("mainPersistenceUnit")
            .build()!!

        @Primary
        @Bean("mainTransactionManager")
        fun mainTransactionManager(mainEntityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
            // Fix "no transactions is in progress": https://stackoverflow.com/a/33397173
            return JpaTransactionManager(mainEntityManagerFactory)
        }
    }

    @Configuration
    @EnableJpaRepositories(
        basePackages = ["org.springframework.batch.core.migration"],
        entityManagerFactoryRef = "batchEntityManagerFactory",
        transactionManagerRef = "batchTransactionManager"
    )
    class Batch {

        @Bean("batchEntityManagerFactory")
        fun batchEntityManagerFactory(
            builder: EntityManagerFactoryBuilder,
            @Qualifier("batchDataSource") dataSource: DataSource,
        ) = builder
            .dataSource(dataSource)
            .packages("org.springframework.batch.core.migration")
            .persistenceUnit("batchPersistenceUnit")
            .build()!!

        @Bean("batchTransactionManager")
        fun batchTransactionManager(@Qualifier("batchDataSource") dataSource: DataSource): PlatformTransactionManager {
            return DataSourceTransactionManager(dataSource)
        }
    }

    companion object {

        const val DRIVER_CLASS_NAME = "org.sqlite.JDBC"
    }
}
