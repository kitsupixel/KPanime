package pt.kitsupixel.kpanime

import android.app.*
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
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
import pt.kitsupixel.kpanime.domain.EpisodeAndShow
import pt.kitsupixel.kpanime.network.DTObjects.NetworkEpisodeContainer
import pt.kitsupixel.kpanime.network.DTObjects.asDomainModel
import pt.kitsupixel.kpanime.repository.ShowsRepository
import pt.kitsupixel.kpanime.ui.main.MainActivity
import pt.kitsupixel.kpanime.work.RefreshDataWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit


class KPApplication : Application() {

    val SUMMARY_ID = 0

    val GROUP_KEY_NEW_EPISODE = "pt.kitsupixel.kpanime.NEW_EPISODE"

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
        val pusher = Pusher(BuildConfig.PusherKey, options)


        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Timber.d("State changed from ${change.previousState} to ${change.currentState}")
            }

            override fun onError(
                message: String,
                code: String,
                e: Exception
            ) {
                Timber.e("There was a problem connecting!\ncode: $code\nmessage: $message\nException: $e")
            }
        }, ConnectionState.ALL)

        val channel: Channel = pusher.subscribe("new-show-episode-channel")

        channel.bind("new-show-episode-event") { event ->
            Timber.d("Received event with data: ${event.data}")

            val gson = GsonBuilder().create()
            val newEpisodes = gson.fromJson<NetworkEpisodeContainer>(
                event.data,
                NetworkEpisodeContainer::class.java
            ).asDomainModel()

            if (newEpisodes.isNotEmpty()) {
                applicationScope.launch {
                    withContext(Dispatchers.IO) {
                        val notifications = mutableListOf<EpisodeAndShow>()
                        for (episode in newEpisodes) {
                            val show = showsRepository.getShowObj(episode.show_id)
                            if (show != null && show.favorite) {
                                notifications.add(
                                    EpisodeAndShow(episode, show)
                                )
                            }
                        }

                        createNewEpisodeChannel(
                            notifications
                        )
                    }
                }
            }
        }
    }


    private fun createNewEpisodeChannel(
        notifications: List<EpisodeAndShow>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.new_episode_notification_channel_id),
                getString(R.string.new_episode_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
                description = getString(R.string.new_episode)
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)

            val pendingIntent = NavDeepLinkBuilder(this)
                .setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.main_nav_graph)
                .setDestination(R.id.latestFragment)
                .createPendingIntent()

            val notificationStyle = NotificationCompat.InboxStyle()

            val notificationsToSend = mutableListOf<Notification>()
            for (item in notifications) {
                notificationsToSend.add(
                    NotificationCompat.Builder(
                        applicationContext,
                        applicationContext.getString(R.string.new_episode_notification_channel_id)
                    )
                        .setSmallIcon(R.drawable.ic_logo_foreground)
                        .setContentTitle(item.show.title)
                        .setContentText(
                            String.format(getString(R.string.episode_released), item.episode.number)
                        )
                        .setContentIntent(pendingIntent)
                        .setGroup(GROUP_KEY_NEW_EPISODE)
                        .setAutoCancel(true)
                        .build()
                )

                notificationStyle.addLine(
                    String.format(
                        getString(R.string.new_episode_notification_line),
                        item.show.title,
                        item.episode.number
                    )
                )
            }

            val summaryNotification = NotificationCompat.Builder(
                applicationContext,
                applicationContext.getString(R.string.new_episode_notification_channel_id)
            )
                .setContentTitle(getString(R.string.new_episode))
                .setContentText(notifications.size.toString() + " new episodes released")
                .setSmallIcon(R.drawable.ic_logo_foreground)
                .setStyle(notificationStyle)
                .setGroup(GROUP_KEY_NEW_EPISODE)
                .setGroupSummary(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            for ((index, item) in notificationsToSend.withIndex()) {
                notificationManager.notify(notifications[index].episode.id.toInt(), item)
            }
            notificationManager.notify(SUMMARY_ID, summaryNotification)
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