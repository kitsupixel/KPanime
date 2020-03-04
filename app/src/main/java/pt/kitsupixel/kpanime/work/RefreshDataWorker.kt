package pt.kitsupixel.kpanime.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bumptech.glide.load.HttpException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import pt.kitsupixel.kpanime.database.getDatabase
import pt.kitsupixel.kpanime.repository.ShowsRepository
import timber.log.Timber

class RefreshDataWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "RefreshDataWorker"
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

        Timber.d("Starting RefreshDataWorker")

        return try {
            repository.refreshShows()
            repository.refreshLatest()
            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        }
    }
}