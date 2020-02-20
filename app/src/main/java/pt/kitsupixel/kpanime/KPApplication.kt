package pt.kitsupixel.kpanime

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceManager
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

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    private lateinit var showsRepository: ShowsRepository

    private lateinit var preferences: SharedPreferences

    private lateinit var preferencesListener: SharedPreferences.OnSharedPreferenceChangeListener

    private val THEME_PREFERENCE = "theme_preference"

    private fun delayedInit() {
        applicationScope.launch {
            showsRepository.refreshShows()

            setupRecurringWork()

            MobileAds.initialize(applicationContext, "ca-app-pub-7666356884507044~9085371469")
        }
    }

    private fun setupRecurringWork() {
        val constraints = Constraints.Builder()
            //.setRequiredNetworkType(NetworkType.UNMETERED)
            //.setRequiresBatteryNotLow(true)
            //.setRequiresCharging(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setRequiresDeviceIdle(true)
                }
            }.build()

        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(5, TimeUnit.SECONDS)
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

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        preferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            Timber.i("SharedPreferences changed: %s", key)
            if (key == THEME_PREFERENCE) {
                setAppTheme(sharedPreferences.getString(key, "system"))
            }
        }

        setAppTheme(preferences.getString(THEME_PREFERENCE, "system"))

        delayedInit()
    }

    fun setAppTheme(theme: String?) {
        val mode = when(theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

}