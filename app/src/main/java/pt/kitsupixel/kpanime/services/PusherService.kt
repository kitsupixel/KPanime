package pt.kitsupixel.kpanime.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.navigation.NavDeepLinkBuilder
import com.google.gson.GsonBuilder
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.database.getDatabase
import pt.kitsupixel.kpanime.domain.EpisodeAndShow
import pt.kitsupixel.kpanime.network.DTObjects.NetworkEpisodeContainer
import pt.kitsupixel.kpanime.network.DTObjects.asDomainModel
import pt.kitsupixel.kpanime.repository.ShowsRepository
import pt.kitsupixel.kpanime.ui.detail.DetailActivity
import pt.kitsupixel.kpanime.ui.main.MainActivity
import timber.log.Timber

class PusherService: Service() {

    val SUMMARY_ID = 0

    val GROUP_KEY_NEW_EPISODE = "pt.kitsupixel.kpanime.NEW_EPISODE"

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    private lateinit var showsRepository: ShowsRepository

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val database = getDatabase(applicationContext)
        showsRepository = ShowsRepository(database)

        setupPusher()
    }

    private fun setupPusher() {
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
            handleEvent(event)
        }
    }

    private fun handleEvent(event: PusherEvent) {
        // Convert message to a list of episodes
        val gson = GsonBuilder().create()
        val newEpisodes = gson.fromJson<NetworkEpisodeContainer>(
            event.data,
            NetworkEpisodeContainer::class.java
        ).asDomainModel()

        if (newEpisodes.isNotEmpty()) {
            applicationScope.launch {
                withContext(Dispatchers.IO) {
                    val notifications = mutableListOf<EpisodeAndShow>()
                    // Check if new episodes are from the selected favorites shows
                    for (episode in newEpisodes) {
                        val show = showsRepository.getShowObj(episode.show_id)
                        if (show != null && show.favorite) {
                            notifications.add(
                                EpisodeAndShow(episode, show)
                            )
                        }
                    }

                    // Create the notifications
                    if (notifications.size > 0) {
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

            // Creates the notification channel
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


            // creates the intent for the summary click notification
            val summaryIntent = NavDeepLinkBuilder(this)
                .setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.main_nav_graph)
                .setDestination(R.id.latestFragment)
                .createPendingIntent()

            val summaryStyle = NotificationCompat.InboxStyle()

            val notificationsToSend = mutableListOf<Notification>()
            for (item in notifications) {
                val resultIntent = Intent(applicationContext, DetailActivity::class.java)
                    .putExtra("showId", item.show.id)

                val pendingIntent = TaskStackBuilder.create(applicationContext).run {
                    addNextIntentWithParentStack(resultIntent)
                    getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
                }
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

                // Adds the show: episode to the summary
                summaryStyle.addLine(
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
                .setStyle(summaryStyle)
                .setGroup(GROUP_KEY_NEW_EPISODE)
                .setGroupSummary(true)
                .setContentIntent(summaryIntent)
                .setAutoCancel(true)
                .build()

            for ((index, item) in notificationsToSend.withIndex()) {
                notificationManager.notify(notifications[index].episode.id.toInt(), item)
            }
            notificationManager.notify(SUMMARY_ID, summaryNotification)
        }
    }
}