package pt.kitsupixel.kpanime.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.database.AppDatabase
import pt.kitsupixel.kpanime.database.entities.*
import pt.kitsupixel.kpanime.domain.Episode
import pt.kitsupixel.kpanime.domain.EpisodeAndShow
import pt.kitsupixel.kpanime.domain.Link
import pt.kitsupixel.kpanime.domain.Show
import pt.kitsupixel.kpanime.network.DTObjects.asDatabaseModel
import pt.kitsupixel.kpanime.network.Network
import retrofit2.HttpException
import timber.log.Timber

class ShowsRepository(private val database: AppDatabase) {

    val shows: LiveData<List<Show>> =
        Transformations.map(database.showDao.get()) {
            it?.showMetaAsDomainModel()
        }

    val favorites: LiveData<List<Show>> =
        Transformations.map(database.showDao.favorites()) {
            it?.showMetaAsDomainModel()
        }

    val current: LiveData<List<Show>> =
        Transformations.map(database.showDao.current()) {
            it?.showMetaAsDomainModel()
        }

    val latest: LiveData<List<EpisodeAndShow>> =
        Transformations.map(database.episodeDao.getLatest()) {
            it?.episodeShowAsDomainModel()
        }

    fun getShow(showId: Long): LiveData<Show?> {
        return Transformations.map(database.showDao.get(showId)) {
            it?.showMetaAsDomainModel()
        }
    }

    fun getEpisodesByShow(showId: Long): LiveData<List<Episode>?> {
        return Transformations.map(database.episodeDao.getByShow(showId)) {
            it?.episodeMetaAsDomainModel()
        }
    }

    fun getEpisode(episodeId: Long): LiveData<Episode?> {
        return Transformations.map(database.episodeDao.get(episodeId)) {
            it?.episodeMetaAsDomainModel()
        }
    }


    fun getLinks(episodeId: Long): LiveData<List<Link>?> {
        return Transformations.map(database.linkDao.getByEpisode(episodeId)) {
            it?.linkAsDomainModel()
        }
    }

    suspend fun refreshShows() {
        if (BuildConfig.Logging) Timber.i("RefreshShows called!")
        withContext(Dispatchers.IO) {
            try {
                val shows = Network.KPanime.getShows().await()
                database.showDao.insert(*shows.asDatabaseModel())
            } catch (e: HttpException) {
                // Log exception //
                Timber.e(e.message())
            } catch (e: Throwable) {
                // Log error //)
            }
        }
    }

    suspend fun refreshEpisodes(showId: Long) {
        if (BuildConfig.Logging) Timber.i("refreshEpisodes called!")
        withContext(Dispatchers.IO) {
            try {
                val episodes = Network.KPanime.getEpisodes(showId).await()
                database.episodeDao.insert(*episodes.asDatabaseModel())
            } catch (e: HttpException) {
                // Log exception //
                e.printStackTrace()
            } catch (e: Throwable) {
                // Log error //)
                e.printStackTrace()
            }
        }
    }

    suspend fun refreshLatest() {
        if (BuildConfig.Logging) Timber.i("refreshLatest called!")
        withContext(Dispatchers.IO) {
            try {
                val episodes = Network.KPanime.getLatestEpisodes().await()
                database.episodeDao.insert(*episodes.asDatabaseModel())
            } catch (e: HttpException) {
                // Log exception //
                Timber.e(e.message())
            } catch (e: Throwable) {
                // Log error //)
            }
        }
    }

    suspend fun refreshLinks(showId: Long?, episodeId: Long?) {
        if (BuildConfig.Logging) Timber.i("refreshLinks called!")
        if (showId != null && episodeId != null) {
            withContext(Dispatchers.IO) {
                try {
                    val links = Network.KPanime.getLinks(showId, episodeId).await()
                    database.linkDao.insert(*links.asDatabaseModel())
                } catch (e: HttpException) {
                    // Log exception //
                    Timber.e(e.message())
                } catch (e: Throwable) {
                    // Log error //)
                }
            }
        }
    }

    suspend fun toggleFavorite(showId: Long) {
        if (BuildConfig.Logging) Timber.i("toggleFavorite called with id $showId!")
        withContext(Dispatchers.IO) {
            val record = database.showMetaDao.get(showId)
            if (record != null) {
                record.favorite = !record.favorite
                database.showMetaDao.update(record)
            } else {
                database.showMetaDao.insert(
                    DatabaseShowMeta(
                        show_id = showId,
                        favorite = true,
                        watched = false
                    )
                )
            }
        }
    }

    suspend fun toggleWatched(showId: Long) {
        if (BuildConfig.Logging) Timber.i("toggleWatched called with id $showId!")
        return withContext(Dispatchers.IO) {
            val record = database.showMetaDao.get(showId)
            if (record != null) {
                record.watched = !record.watched
                database.showMetaDao.update(record)
            } else {
                database.showMetaDao.insert(
                    DatabaseShowMeta(
                        show_id = showId,
                        favorite = false,
                        watched = true
                    )
                )
            }
        }
    }

    suspend fun toggleEpisodeDownloaded(episodeId: Long, value: Boolean?): Boolean {
        if (BuildConfig.Logging) Timber.i("toggleWatched called with id $episodeId!")
        return withContext(Dispatchers.IO) {
            val record = database.episodeMetaDao.get(episodeId)
            if (record != null) {
                record.downloaded = !record.downloaded
                if (value != null) record.downloaded = value
                database.episodeMetaDao.update(record)

                record.downloaded
            } else {
                database.episodeMetaDao.insert(
                    DatabaseEpisodeMeta(
                        episode_id = episodeId,
                        watched = false,
                        downloaded = true
                    )
                )

                true
            }
        }
    }

    suspend fun toggleEpisodeWatched(episodeId: Long, value: Boolean?): Boolean {
        if (BuildConfig.Logging) Timber.i("toggleWatched called with id $episodeId!")
        return withContext(Dispatchers.IO) {
            val record = database.episodeMetaDao.get(episodeId)
            if (record != null) {
                record.watched = !record.watched
                if (value != null) record.downloaded = value
                database.episodeMetaDao.update(record)

                record.watched
            } else {
                database.episodeMetaDao.insert(
                    DatabaseEpisodeMeta(
                        episode_id = episodeId,
                        watched = true,
                        downloaded = false
                    )
                )

                true
            }
        }
    }
}