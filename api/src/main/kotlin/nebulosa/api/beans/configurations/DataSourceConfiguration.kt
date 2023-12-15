package nebulosa.api.beans.configurations

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class DataSourceConfiguration {

    @Value("\${spring.datasource.url}") private lateinit var dataSourceUrl: String

    @Bean
    @Primary
    fun dataSource(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = dataSourceUrl
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

        @Bean
        @Primary
        fun entityManagerFactory(
            builder: EntityManagerFactoryBuilder,
            dataSource: DataSource,
        ) = builder
            .dataSource(dataSource)
            .packages("nebulosa.api")
            .persistenceUnit("persistenceUnit")
            .build()!!

        @Bean
        @Primary
        fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
            // Fix "no transactions is in progress": https://stackoverflow.com/a/33397173
            return JpaTransactionManager(entityManagerFactory)
        }
    }

    companion object {

        const val DRIVER_CLASS_NAME = "org.sqlite.JDBC"
    }
}
