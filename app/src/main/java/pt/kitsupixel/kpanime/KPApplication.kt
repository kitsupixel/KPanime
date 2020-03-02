package pt.kitsupixel.kpanime

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.ads.MobileAds
import com.google.gson.GsonBuilder
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.kitsupixel.kpanime.database.getDatabase
import pt.kitsupixel.kpanime.network.DTObjects.NetworkEpisodeContainer
import pt.kitsupixel.kpanime.repository.ShowsRepository
import pt.kitsupixel.kpanime.utils.sendNotification
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

            if (showsRepository.isDBEmpty()) {
                withContext(Dispatchers.Unconfined) {
                    showsRepository.refreshShows()
                }
            }

            withContext(Dispatchers.Unconfined) {
                showsRepository.refreshLatest()
            }

            //setupRecurringWork()

            MobileAds.initialize(applicationContext, "ca-app-pub-7666356884507044~9085371469")

            setupNotificationChannel()
        }
    }

    private fun setupNotificationChannel() {
        val options = PusherOptions()
        options.setCluster("eu")
        val pusher = Pusher("fe66ce92d8502e96f15c", options)


        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Timber.d("State changed from ${change.previousState} to ${change.currentState}")
            }

            override fun onError(
                message: String,
                code: String,
                e: Exception
            ) {
                Timber.e(
                    """There was a problem connecting! 
code: $code
message: $message
Exception: $e"""
                )
            }
        }, ConnectionState.ALL)

        val channel: Channel = pusher.subscribe("new-show-episode-channel")

        channel.bind("new-show-episode-event") { event ->
            Timber.d("Received event with data: ${event.data}")

            val gson = GsonBuilder().create()
            val result = gson.fromJson<NetworkEpisodeContainer>(
                event.data,
                NetworkEpisodeContainer::class.java
            )

            val newEpisodes = result.data
            if (newEpisodes.isNotEmpty()) {
                applicationScope.launch {
                    withContext(Dispatchers.IO) {
                        var message: String = ""
                        for (episode in newEpisodes) {
                            val show = showsRepository.getShowObj(episode.show_id)
                            if (show != null && show.favorite) {
                                message += "${show.title} : New Episode ${episode.number}\n"
                            }
                        }
                        createChannel(
                            getString(R.string.new_episode_notification_channel_id),
                            getString(R.string.new_episode_notification_channel_name),
                            message
                        )
                    }
                }
            }

            Timber.d(result.toString())
        }
    }

    private fun createChannel(channelId: String, channelName: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                // TODO: Step 2.4 change importance
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(true)
            }
            // TODO: Step 2.6 disable badges for this channel

            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.new_episode)

            val notificationManager = this.getSystemService(
                NotificationManager::class.java
            )

            notificationManager?.createNotificationChannel(notificationChannel)
            // TODO: Step 1.6 END create channel
            notificationManager?.sendNotification(message, this)
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

        preferencesListener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                Timber.i("SharedPreferences changed: %s", key)
                if (key == THEME_PREFERENCE) {
                    setAppTheme(sharedPreferences.getString(key, "system"))
                }
            }

        setAppTheme(preferences.getString(THEME_PREFERENCE, "system"))

        delayedInit()
    }

    fun setAppTheme(theme: String?) {
        val mode = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

}