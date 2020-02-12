package pt.kitsupixel.kpanime.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bumptech.glide.load.HttpException
import pt.kitsupixel.kpanime.database.getDatabase
import pt.kitsupixel.kpanime.domain.EpisodeAndShow
import pt.kitsupixel.kpanime.repository.ShowsRepository
import timber.log.Timber

class RefreshDataWorker(appContext: Context, params: WorkerParameters):
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "RefreshDataWorker"
    }

    /**
     * A coroutine-friendly method to do your work.
     */

    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = ShowsRepository(database)

        return try {
            repository.refreshShows()
            repository.refreshLatest()
            val latest = repository.latest
            val latestList: List<EpisodeAndShow>? = latest.value

            if (latestList?.size!! > 0) {
                for (episodeAndShow in latestList) {
                    if (episodeAndShow.show.favorite == true) {
                        Timber.i("New episode %s from your favorites found! %s", episodeAndShow.episode.number, episodeAndShow.show.title)
                    } else {
                        Timber.i("Not from your favorites %s : %s", episodeAndShow.episode.number, episodeAndShow.show.title)
                    }
                }
            }

            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        }
    }
}