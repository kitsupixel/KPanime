package pt.kitsupixel.kpanime.ui.episode

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pt.kitsupixel.kpanime.database.getDatabase
import pt.kitsupixel.kpanime.repository.ShowsRepository

class EpisodeViewModel(application: Application, showId: Long, episodeId: Long) : ViewModel() {
    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val database = getDatabase(application)
    private val showsRepository = ShowsRepository(database)

    var episode = showsRepository.getEpisodeAndLinks(episodeId)

    init {
        viewModelScope.launch {
            showsRepository.refreshLinks(showId, episodeId)
        }
    }

    class Factory(val app: Application, val showId: Long, val episodeId: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EpisodeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EpisodeViewModel(app, showId, episodeId) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
