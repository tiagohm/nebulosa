package nebulosa.api.beans.configurations

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class DataSourceConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    fun dataSource(): DataSource {
        return DriverManagerDataSource()
    }

    @Bean("batchDataSource")
    fun batchDataSource(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = JDBC_MEMORY_URL
        config.driverClassName = DRIVER_CLASS_NAME
        config.maximumPoolSize = 1
        config.minimumIdle = 1
        return HikariDataSource(config)
    }

    @Configuration
    @EnableJpaRepositories(
        basePackages = ["nebulosa.api"],
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
    )
    class Main {

        @Primary
        @Bean(name = ["entityManagerFactory"])
        fun entityManagerFactory(
            builder: EntityManagerFactoryBuilder,
            dataSource: DataSource,
        ) = builder
            .dataSource(dataSource)
            .packages("nebulosa.api")
            .persistenceUnit("mainPersistenceUnit")
            .build()!!

        @Bean
        @Primary
        fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
            // Fix "no transactions is in progress": https://stackoverflow.com/a/33397173
            return JpaTransactionManager(entityManagerFactory)
        }
    }

    @Configuration
    @EnableJpaRepositories(
        basePackages = ["org.springframework.batch.core.migration"],
        entityManagerFactoryRef = "batchEntityManagerFactory",
        transactionManagerRef = "batchTransactionManager"
    )
    class Batch {

        @Bean(name = ["batchEntityManagerFactory"])
        fun batchEntityManagerFactory(
            builder: EntityManagerFactoryBuilder,
            @Qualifier("batchDataSource") dataSource: DataSource,
        ) = builder
            .dataSource(dataSource)
            .packages("org.springframework.batch.core.migration")
            .persistenceUnit("batchPersistenceUnit")
            .build()!!

        @Bean
        fun batchTransactionManager(@Qualifier("batchDataSource") dataSource: DataSource): PlatformTransactionManager {
            return DataSourceTransactionManager(dataSource)
        }
    }

    companion object {

        const val DRIVER_CLASS_NAME = "org.sqlite.JDBC"
        const val JDBC_MEMORY_URL = "jdbc:sqlite:file:nebulosa.db?mode=memory"
    }
}
