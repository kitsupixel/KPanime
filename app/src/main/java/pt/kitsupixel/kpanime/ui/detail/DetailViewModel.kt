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
import pt.kitsupixel.kpanime.repository.ShowsRepository
import timber.log.Timber

class DetailViewModel(application: Application, private val showId: Long) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val database = getDatabase(application)
    private val showsRepository = ShowsRepository(database)

    var show = showsRepository.getShow(showId)

    val episodes = showsRepository.getEpisodesByShow(showId)

    private val _refreshing = MutableLiveData<Boolean>(false)
    val refreshing: LiveData<Boolean>
        get() = _refreshing

    private val _eventFavorite = MutableLiveData<Boolean?>()
    val eventFavorite: LiveData<Boolean?>
        get() = _eventFavorite


    init {
        Timber.i("Init")
        viewModelScope.launch {
            _refreshing.value = true
            showsRepository.refreshEpisodes(showId)
            _refreshing.value = false
        }
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

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            showsRepository.refreshEpisodes(showId)
            showsRepository.refreshShows()
            _refreshing.value = false
        }
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
