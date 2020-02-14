package pt.kitsupixel.kpanime.ui.episode

import android.app.Application
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frostwire.jlibtorrent.AlertListener
import com.frostwire.jlibtorrent.alerts.Alert
import com.frostwire.jlibtorrent.alerts.Alerts
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
import pt.kitsupixel.kpanime.KPApplication
import pt.kitsupixel.kpanime.database.getDatabase
import pt.kitsupixel.kpanime.repository.ShowsRepository
import pt.kitsupixel.kpanime.utils.humanReadableByteCountSI
import timber.log.Timber

class EpisodeViewModel(
    private val application: Application,
    private val showId: Long,
    private val episodeId: Long
) : ViewModel(),
    TorrentListener, AlertListener {
    private val viewModelJob = SupervisorJob()
    override fun alert(alert: Alert<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun types(): IntArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val database = getDatabase(application)
    private val showsRepository = ShowsRepository(database)

    var episode = showsRepository.getEpisode(episodeId)

    var links = showsRepository.getLinks(episodeId)

    private val _refreshing = MutableLiveData<Boolean>(false)
    val refreshing: LiveData<Boolean>
        get() = _refreshing

    // Buttons
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

    // Torrent
    private var _loadingTorrent = MutableLiveData<Boolean>(false)
    val loadingTorrent: LiveData<Boolean>
        get() = _loadingTorrent

    private var _openPlayer = MutableLiveData<Boolean>(false)
    val openPlayer: LiveData<Boolean>
        get() = _openPlayer

    private var _progressTorrent = MutableLiveData(0)
    val progressTorrent: LiveData<Int>
        get() = _progressTorrent

    private var _progressTorrentText = MutableLiveData<String?>("Connecting...")
    val progressTorrentText: LiveData<String?>
        get() = _progressTorrentText

    private var _realProgressTorrent = MutableLiveData(0)
    val realProgressTorrent: LiveData<Int>
        get() = _realProgressTorrent

    private var _realProgressTorrentText = MutableLiveData<String>("Seeds: 0")
    val realProgressTorrentText: LiveData<String>
        get() = _realProgressTorrentText

    var torrentStream: TorrentStream


    private var downloadInBackground = false

    init {
        viewModelScope.launch {
            refresh()
        }

        val torrentOptions = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            TorrentOptions.Builder()
                .saveLocation(MediaStore.Downloads.EXTERNAL_CONTENT_URI.toString())
                .removeFilesAfterStop(false)
                .autoDownload(true)
                .build()
        } else {
            TorrentOptions.Builder()
                .saveLocation(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
                .removeFilesAfterStop(false)
                .autoDownload(true)
                .build()
        }


        torrentStream = TorrentStream.init(torrentOptions)
        torrentStream.addListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        if (!downloadInBackground) {
            torrentStream.stopStream()
            (application as KPApplication).setIsDownloading(false)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            showsRepository.refreshLinks(showId, episodeId)
            _refreshing.value = false
        }
    }

    fun setTorrentOptions(removeAfterStop: Boolean) {
        Timber.i("setTorrentOptions: $removeAfterStop")

        torrentStream.options = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            TorrentOptions.Builder()
                .saveLocation(MediaStore.Downloads.EXTERNAL_CONTENT_URI.toString())
                .removeFilesAfterStop(removeAfterStop)
                .build()
        } else {
            TorrentOptions.Builder()
                .saveLocation(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
                .removeFilesAfterStop(removeAfterStop)
                .build()
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

    fun clearTorrent480Link() {
        _torrent480.value = null
    }

    fun clearTorrent720Link() {
        _torrent720.value = null
    }

    fun clearTorrent1080Link() {
        _torrent1080p.value = null
    }

    fun setTextViewable(value: Boolean) {
        _textViewable.value = value
    }

    fun startStream(url: String) {
        torrentStream.startStream(url)
        _loadingTorrent.value = true
    }

    fun endLoading() {
        _loadingTorrent.value = false
        _progressTorrent.value = 0
    }

    // TORRENT HANDLING
    private var _torrent = MutableLiveData<Torrent?>(null)
    val torrent: LiveData<Torrent?>
        get() = _torrent

    override fun onStreamReady(torrent: Torrent?) {
        if (BuildConfig.Logging) Timber.i("onStreamReady")
        _progressTorrent.value = 100
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
        (application as KPApplication).setIsDownloading(true)
    }

    override fun onStreamProgress(torrent: Torrent?, status: StreamStatus?) {
        if (status != null && _progressTorrent.value != status.bufferProgress) {
            if (BuildConfig.Logging) Timber.i("Progress: %s", status.bufferProgress)
            _progressTorrent.value = status.bufferProgress

        }
        if (status != null) {
            if (BuildConfig.Logging) Timber.i("RealProgress: %f", status.progress)
            _realProgressTorrent.value = status.progress.toInt()
            _realProgressTorrentText.value = "Seeds: ${status.seeds}"
            _progressTorrentText.value =
                "Down. Speed: ${humanReadableByteCountSI(status.downloadSpeed.toLong())}"
            if (status.progress in 99.85f..100f) {
                torrentStream.stopStream()
                endLoading()
                (application as KPApplication).setIsDownloading(false)
                Timber.i("DOWNLOAD FINISHED!!!!")
            }
        }

    }

    override fun onStreamError(torrent: Torrent?, e: Exception?) {
        if (BuildConfig.Logging) Timber.e("onStreamError")
        e?.printStackTrace()
    }

    fun setDownloadInBackground() {
        downloadInBackground = true
        (application as KPApplication).setIsDownloading(true)
        endLoading()
    }

    fun getIsDownloadInBackground(): Boolean {
        return downloadInBackground
    }

    fun clearTorrentButtons() {
        clearTorrent480Link()
        clearTorrent720Link()
        clearTorrent1080Link()
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
