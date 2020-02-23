package pt.kitsupixel.kpanime.ui.detail

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pt.kitsupixel.kpanime.database.getDatabase
import pt.kitsupixel.kpanime.domain.Episode
import pt.kitsupixel.kpanime.domain.Show
import pt.kitsupixel.kpanime.repository.ShowsRepository
import timber.log.Timber

class DetailViewModel(application: Application, private val showId: Long) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val database = getDatabase(application)
    private val showsRepository = ShowsRepository(database)

    var show: LiveData<Show?>

    var episodes: LiveData<List<Episode>?>

    private val _refreshing = MutableLiveData<Boolean>(false)
    val refreshing: LiveData<Boolean>
        get() = _refreshing

    private val _eventFavorite = MutableLiveData<Boolean?>()
    val eventFavorite: LiveData<Boolean?>
        get() = _eventFavorite

    private val _eventWatched = MutableLiveData<Boolean?>()
    val eventWatched: LiveData<Boolean?>
        get() = _eventWatched

    private val _eventEpisodeWatched = MutableLiveData<Boolean?>()
    val eventEpisodeWatched: LiveData<Boolean?>
        get() = _eventEpisodeWatched

    private val _eventEpisodeDownloaded = MutableLiveData<Boolean?>()
    val eventEpisodeDownloaded: LiveData<Boolean?>
        get() = _eventEpisodeDownloaded


    init {
        Timber.i("Init")
        _refreshing.value = true

        show = showsRepository.getShow(showId)
        episodes = showsRepository.getEpisodesByShow(showId)

        viewModelScope.launch {
            showsRepository.refreshEpisodes(showId)
        }

        _refreshing.value = false
    }

    fun toggleFavorite() {
        val toggle = show.value?.favorite ?: false
        viewModelScope.launch {
            showsRepository.toggleFavorite(showId)
            _eventFavorite.value = toggle == false
        }
    }

    fun eventFavoriteClear() {
        _eventFavorite.value = null
    }

    fun toggleWatched() {
        val toggle = show.value?.watched ?: false
        viewModelScope.launch {
            showsRepository.toggleWatched(showId)
            _eventWatched.value = toggle == false
        }
    }

    fun eventWatchedClear() {
        _eventEpisodeWatched.value = null
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            showsRepository.refreshEpisodes(showId)
            //showsRepository.refreshShows()
            _refreshing.value = false
        }
    }

    fun toggleEpisodeWatched(episodeId: Long) {
        viewModelScope.launch {
            _eventEpisodeWatched.value = showsRepository.toggleEpisodeWatched(episodeId, null)
        }
    }

    fun eventEpisodeWatchedClear() {
        _eventEpisodeWatched.value = null
    }

    fun toggleEpisodeDownloaded(episodeId: Long) {
        viewModelScope.launch {
            _eventEpisodeDownloaded.value = showsRepository.toggleEpisodeDownloaded(episodeId, null)
        }
    }

    fun eventEpisodeDownloadedClear() {
        _eventEpisodeDownloaded.value = null
    }

    class Factory(val app: Application, val id: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DetailViewModel(app, id) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
