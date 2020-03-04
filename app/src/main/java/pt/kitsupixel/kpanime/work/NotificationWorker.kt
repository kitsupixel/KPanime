package pt.kitsupixel.kpanime.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bumptech.glide.load.HttpException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.database.getDatabase
import pt.kitsupixel.kpanime.domain.EpisodeAndShow
import pt.kitsupixel.kpanime.repository.ShowsRepository
import pt.kitsupixel.kpanime.ui.detail.DetailActivity
import pt.kitsupixel.kpanime.ui.main.MainActivity
import timber.log.Timber

class NotificationWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "NotificationWorker"
        const val SUMMARY_ID = 0
        const val GROUP_KEY_NEW_EPISODE = "pt.kitsupixel.kpanime.NEW_EPISODE"
    }

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    /**
     * A coroutine-friendly method to do your work.
     */

    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = ShowsRepository(database)

        Timber.d("Starting NotificationWorker")

        return try {
            withContext(applicationScope.coroutineContext) {
                val notifications = mutableListOf<EpisodeAndShow>()

                var insertedIds = repository.refreshLatest()

                if (BuildConfig.DEBUG)
                    insertedIds = mutableListOf(21135, 21134)

                Timber.d("InsertedIds: ${insertedIds.toString()}")

                if (insertedIds != null) {
                    for (episodeId in insertedIds) {
                        val episode = repository.getEpisodeObj(episodeId)
                        Timber.d("Episode: %s", episode?.number.toString())
                        if (episode != null) {
                            val show = repository.getShowObj(episode.show_id)
                            Timber.d("Show: %s", show?.title.toString())
                            if (show != null) {
                                Timber.d("New notification! %s : %s", show.title, episode.number)
                                notifications.add(
                                    EpisodeAndShow(episode, show)
                                )
//                                } else if (show != null) {
//                                    Timber.d("New episode but no notification! %s : %s", show.title, episode.number)
                            }
                        }
                    }
                }

                if (notifications.size > 0) {
                    createNewEpisodeChannel(notifications)
                }

                Result.success()
            }
        } catch (e: HttpException) {
            Result.retry()
        }
    }

    private fun createNewEpisodeChannel(
        notifications: List<EpisodeAndShow>
    ) {
        Timber.d("Creating %d notifications", notifications.size)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Creates the notification channel
            val channel = NotificationChannel(
                applicationContext.getString(R.string.new_episode_notification_channel_id),
                applicationContext.getString(R.string.new_episode_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
                description = applicationContext.getString(R.string.new_episode)
            }

            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)


            // creates the intent for the summary click notification
            val summaryIntent = NavDeepLinkBuilder(applicationContext)
                .setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.main_nav_graph)
                .setDestination(R.id.latestFragment)
                .createPendingIntent()

            val summaryStyle = NotificationCompat.InboxStyle()

            val notificationsToSend = mutableListOf<Notification>()
            for (item in notifications) {
                val resultIntent = Intent(applicationContext, DetailActivity::class.java)
                    .putExtra("showId", item.show.id)
                    .setAction(System.currentTimeMillis().toString())

                val pendingIntent = TaskStackBuilder.create(applicationContext).run {
                    addNextIntentWithParentStack(resultIntent)
                    getPendingIntent(item.show.id.toInt(), PendingIntent.FLAG_UPDATE_CURRENT)
                }
                notificationsToSend.add(
                    NotificationCompat.Builder(
                        applicationContext,
                        applicationContext.getString(R.string.new_episode_notification_channel_id)
                    )
                        .setSmallIcon(R.drawable.ic_logo_foreground)
                        .setContentTitle(item.show.title)
                        .setContentText(
                            String.format(
                                applicationContext.getString(R.string.episode_released),
                                item.episode.number
                            )
                        )
                        .setContentIntent(pendingIntent)
                        .setGroup(GROUP_KEY_NEW_EPISODE)
                        .setAutoCancel(true)
                        .build()
                )

                // Adds the show: episode to the summary
                summaryStyle.addLine(
                    String.format(
                        applicationContext.getString(R.string.new_episode_notification_line),
                        item.show.title,
                        item.episode.number
                    )
                )
            }

            val summaryNotification = NotificationCompat.Builder(
                applicationContext,
                applicationContext.getString(R.string.new_episode_notification_channel_id)
            )
                .setContentTitle(applicationContext.getString(R.string.new_episode))
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