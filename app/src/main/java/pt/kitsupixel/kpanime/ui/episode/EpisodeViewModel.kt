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


    private var _torrent480 = MutableLiveData<String?>()
    val torrent480p: LiveData<String?>
        get() = _torrent480

    private var _torrent720 = MutableLiveData<String?>()
    val torrent720p: LiveData<String?>
        get() = _torrent720

    private var _torrent1080p = MutableLiveData<String?>()
    val torrent1080p: LiveData<String?>
        get() = _torrent1080p

    private var _textViewable = MutableLiveData<Boolean>(false)
    val textViewable: LiveData<Boolean>
        get() = _textViewable


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

    fun setTorrent480Link(url: String) {
        _torrent480.value = url
    }

    fun setTorrent720Link(url: String) {
        _torrent720.value = url
    }

    fun setTorrent1080Link(url: String) {
        _torrent1080p.value = url
    }

    fun setTextViewable(value: Boolean) {
        _textViewable.value = value
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
