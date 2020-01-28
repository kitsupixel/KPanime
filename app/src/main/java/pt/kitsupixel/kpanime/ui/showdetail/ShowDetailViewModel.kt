package pt.kitsupixel.kpanime.ui.showdetail

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

class ShowDetailViewModel(application: Application, showId: Long) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val database = getDatabase(application)
    private val showsRepository = ShowsRepository(database)

    var show = showsRepository.getShow(showId)

    val episodes = showsRepository.getEpisodesByShow(showId)

    private val _eventFavorite = MutableLiveData<Boolean?>()
    val eventFavorite: LiveData<Boolean?>
        get() = _eventFavorite


    init {
        viewModelScope.launch {
            showsRepository.refreshEpisodes(showId)
        }
    }

    fun toggleFavorite() {
        val id = show.value?.id ?: 0L
        val toggle = show.value?.favorite ?: false
        viewModelScope.launch {
            showsRepository.toggleFavorite(id)
            _eventFavorite.value = toggle == false
        }
    }

    fun eventFavoriteClear() {
        _eventFavorite.value = null
    }

    class Factory(val app: Application, val id: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShowDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ShowDetailViewModel(app, id) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}