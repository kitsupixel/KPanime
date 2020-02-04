package pt.kitsupixel.kpanime.ui.episode

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

class EpisodeViewModel(private val application: Application, private val showId: Long, private val episodeId: Long) : ViewModel() {
    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val database = getDatabase(application)
    private val showsRepository = ShowsRepository(database)

    var episode = showsRepository.getEpisode(episodeId)
    var links = showsRepository.getLinks(episodeId)

    init {
        viewModelScope.launch {
            refresh()
        }
    }

    private val _refreshing = MutableLiveData<Boolean?>()
    val refreshing: LiveData<Boolean?>
        get() = _refreshing

    fun refresh() {
        _refreshing.value = true
        viewModelScope.launch {
            showsRepository.refreshLinks(showId, episodeId)
        }
        _refreshing.value = false
    }

    class Factory(val app: Application, val showId: Long, val episodeId: Long) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EpisodeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EpisodeViewModel(app, showId, episodeId) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
