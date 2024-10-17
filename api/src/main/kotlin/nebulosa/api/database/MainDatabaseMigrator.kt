package nebulosa.api.database

import org.flywaydb.core.Flyway

data class MainDatabaseMigrator(private val dataSource: String) : Runnable {

    override fun run() {
        Flyway.configure()
            .baselineVersion("0")
            .baselineOnMigrate(true)
            .table("migrations")
            .dataSource(dataSource, "root", "")
            .locations("classpath:migrations/main")
            .load()
            .migrate()
    }
}
