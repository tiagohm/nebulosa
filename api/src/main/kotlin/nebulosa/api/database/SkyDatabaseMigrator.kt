package nebulosa.api.database

import org.flywaydb.core.Flyway
import java.util.concurrent.CountDownLatch

data class SkyDatabaseMigrator(private val dataSource: String) : Runnable {

    private val latch = CountDownLatch(1)

    override fun run() {
        Flyway.configure()
            .baselineVersion("0")
            .baselineOnMigrate(true)
            .table("MIGRATIONS")
            .dataSource(dataSource, "root", "")
            .locations("classpath:migrations/sky")
            .load()
            .migrate()

        latch.countDown()
    }

    fun await() {
        latch.await()
    }
}
