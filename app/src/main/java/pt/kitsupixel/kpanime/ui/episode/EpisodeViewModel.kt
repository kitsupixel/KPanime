package pt.kitsupixel.kpanime.ui.episode

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.github.se_bastiaan.torrentstream.StreamStatus
import com.github.se_bastiaan.torrentstream.Torrent
import com.github.se_bastiaan.torrentstream.TorrentOptions
import com.github.se_bastiaan.torrentstream.TorrentStream
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.database.getDatabase
import pt.kitsupixel.kpanime.repository.ShowsRepository
import pt.kitsupixel.kpanime.utils.humanReadableByteCountSI
import timber.log.Timber

class EpisodeViewModel(
    private val application: Application,
    private val showId: Long,
    private val episodeId: Long
) : ViewModel(),
    TorrentListener {
    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val database = getDatabase(application)
    private val showsRepository = ShowsRepository(database)

    var episode = showsRepository.getEpisode(episodeId)

    var links = showsRepository.getLinks(episodeId)

    private val _refreshing = MutableLiveData<Boolean>(false)
    val refreshing: LiveData<Boolean>
        get() = _refreshing

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

    private var _loadingTorrent = MutableLiveData<Boolean>(false)
    val loadingTorrent: LiveData<Boolean>
        get() = _loadingTorrent

    private var _openPlayer = MutableLiveData<Boolean>(false)
    val openPlayer: LiveData<Boolean>
        get() = _openPlayer

    private var _progressTorrent = MutableLiveData(0)
    val progressTorrent: LiveData<Int>
        get() = _progressTorrent

    private var _progressTorrentText =
        MutableLiveData<String?>(application.resources.getString(R.string.connecting))
    val progressTorrentText: LiveData<String?>
        get() = _progressTorrentText

    var torrentStream: TorrentStream

    init {
        viewModelScope.launch {
            refresh()
        }

        PreferenceManager.getDefaultSharedPreferences(application.applicationContext)
            .getBoolean("delete_episodes_preference", true)
        torrentStream = TorrentStream.init(
            TorrentOptions.Builder()
                .saveLocation(application.getExternalFilesDir(null))
                .removeFilesAfterStop(
                    PreferenceManager.getDefaultSharedPreferences(application.applicationContext).getBoolean(
                        "delete_episodes_preference",
                        true
                    )
                )
                .build()
        )
        torrentStream.addListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        torrentStream.stopStream()
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            showsRepository.refreshLinks(showId, episodeId)
            _refreshing.value = false
        }
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

    fun startStream(url: String) {
        torrentStream.startStream(url)
        _loadingTorrent.value = true
    }

    fun setProgress(value: Int) {
        _progressTorrent.value = value
    }

    fun endLoading() {
        _loadingTorrent.value = false
        setProgress(0)
    }

    // TORRENT HANDLING
    private var _torrent = MutableLiveData<Torrent?>(null)
    val torrent: LiveData<Torrent?>
        get() = _torrent

    override fun onStreamReady(torrent: Torrent?) {
        if (BuildConfig.Logging) Timber.i("onStreamReady")
        setProgress(100)
        _torrent.value = torrent
        _openPlayer.value = true
    }

    override fun onStreamPrepared(torrent: Torrent?) {
        if (BuildConfig.Logging) Timber.i("onStreamPrepared")
    }

    override fun onStreamStopped() {
        if (BuildConfig.Logging) Timber.i("onStreamStopped")
    }

    override fun onStreamStarted(torrent: Torrent?) {
        if (BuildConfig.Logging) Timber.i("onStreamStarted")
    }

    override fun onStreamProgress(torrent: Torrent?, status: StreamStatus?) {
        if (status != null && _progressTorrent.value != status.bufferProgress) {
            if (BuildConfig.Logging) Timber.i("Progress: %s", status.bufferProgress)
            _progressTorrent.value = status.bufferProgress
            _progressTorrentText.value = String.format(
                application.resources.getString(R.string.down_speed),
                humanReadableByteCountSI(status.downloadSpeed.toLong())
            )
        }
    }

    override fun onStreamError(torrent: Torrent?, e: java.lang.Exception?) {
        if (BuildConfig.Logging) Timber.i("onStreamError")
    }

    fun markEpisodeWatched() {
        viewModelScope.launch {
            showsRepository.toggleEpisodeWatched(episodeId, true)
        }
    }

    fun markEpisodeDownloaded() {
        viewModelScope.launch {
            showsRepository.toggleEpisodeDownloaded(episodeId, true)
        }
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
