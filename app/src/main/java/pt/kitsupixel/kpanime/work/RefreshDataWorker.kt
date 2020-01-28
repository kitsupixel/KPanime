package pt.kitsupixel.kpanime.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bumptech.glide.load.HttpException
import pt.kitsupixel.kpanime.database.getDatabase
import pt.kitsupixel.kpanime.repository.ShowsRepository

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
            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        }
    }
}