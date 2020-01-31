package pt.kitsupixel.kpanime

import android.app.Application
import android.os.Build
import androidx.work.*
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.kitsupixel.kpanime.database.getDatabase
import pt.kitsupixel.kpanime.repository.ShowsRepository
import pt.kitsupixel.kpanime.work.RefreshDataWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit

class KPApplication : Application() {

    val applicationScope = CoroutineScope(Dispatchers.Default)

    private fun delayedInit() {
        applicationScope.launch {
            setupRecurringWork()
            showsRepository.refreshShows()
            //showsRepository.refreshLatest()
        }

        if (!BuildConfig.noAds)
            MobileAds.initialize(this, "ca-app-pub-7666356884507044~2530296315")
    }

    private lateinit var showsRepository: ShowsRepository

    private fun setupRecurringWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setRequiresDeviceIdle(true)
                }
            }.build()

        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            RefreshDataWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.Logging)
            Timber.plant(Timber.DebugTree())

        val database = getDatabase(this)
        showsRepository = ShowsRepository(database)

        delayedInit()
    }
}