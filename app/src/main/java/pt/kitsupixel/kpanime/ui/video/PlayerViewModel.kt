package pt.kitsupixel.kpanime.ui.video

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import pt.kitsupixel.kpanime.database.getDatabase
import pt.kitsupixel.kpanime.repository.ShowsRepository
import java.io.IOException

class PlayerViewModel(application: Application) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
//    private val database = getDatabase(application)
//    private val showsRepository = ShowsRepository(database)

    val USE_TEXTURE_VIEW = false
    val ENABLE_SUBTITLES = true

    var mLibVLC: LibVLC = LibVLC(application)
    var mMediaPlayer: MediaPlayer

    private var _time = MutableLiveData<Int>(0)
    val time: LiveData<Int>
        get() = _time

    private var _size = MutableLiveData<Int>(0)
    val size: LiveData<Int>
        get() = _size

    init {
        mMediaPlayer = MediaPlayer(mLibVLC)
    }

    override fun onCleared() {
        super.onCleared()
        mMediaPlayer.stop()
        mMediaPlayer.detachViews()

        mMediaPlayer.release()
        mLibVLC.release()
    }


    fun initializeVideo(videoLayout: VLCVideoLayout, filePath: String) {
        mMediaPlayer.attachViews(videoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW)
        try {
            val media = Media(mLibVLC, filePath)
            mMediaPlayer.media = media
            media.release()
        } catch (e: IOException) {
            throw RuntimeException("Invalid asset folder")
        }

        _size.value = mMediaPlayer.length.toInt()
        _time.value = mMediaPlayer.time.toInt()
        mMediaPlayer.play()
    }

    fun togglePlay() {
        if (mMediaPlayer.isPlaying) mMediaPlayer.pause()
        else mMediaPlayer.play()
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlayerViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
