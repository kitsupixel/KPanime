package pt.kitsupixel.kpanime

import android.system.Os.close
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import pt.kitsupixel.kpanime.database.AppDatabase
import pt.kitsupixel.kpanime.database.MIGRATION_3_4
import pt.kitsupixel.kpanime.database.MIGRATION_4_5
import java.io.IOException
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    // Array of all migrations
    private val ALL_MIGRATIONS = arrayOf(
        MIGRATION_3_4, MIGRATION_4_5)

    @Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create earliest version of the database.
        helper.createDatabase(TEST_DB, 1).apply {
            close()
        }

        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().getTargetContext(),
            Room.,
                    TEST_DB
        ).addMigrations(*ALL_MIGRATIONS).build().apply {
            getOpenHelper().getWritableDatabase()
            close()
        }
    }
}