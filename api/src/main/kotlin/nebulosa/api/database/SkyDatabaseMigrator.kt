package nebulosa.api.database

import org.flywaydb.core.Flyway

data class SkyDatabaseMigrator(private val dataSource: String) : Runnable {

    override fun run() {
        Flyway.configure()
            .baselineVersion("0")
            .baselineOnMigrate(true)
            .table("MIGRATIONS")
            .dataSource(dataSource, "root", "")
            .locations("classpath:migrations/sky")
            .load()
            .migrate()
    }
}
