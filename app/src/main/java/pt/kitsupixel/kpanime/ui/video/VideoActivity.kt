package pt.kitsupixel.kpanime.ui.video

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import pt.kitsupixel.kpanime.R
import timber.log.Timber
import java.io.IOException


class VideoActivity : AppCompatActivity() {

    private val USE_TEXTURE_VIEW = false
    private val ENABLE_SUBTITLES = true
    private var ASSET_FILENAME: String? = null

    private var mVideoLayout: VLCVideoLayout? = null

    private var mLibVLC: LibVLC? = null
    private var mMediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        ASSET_FILENAME = intent.getStringExtra("filePath")

        Timber.i("FileName: $ASSET_FILENAME")

        val args: ArrayList<String> = ArrayList()
        args.add("-vvv")
        mLibVLC = LibVLC(this, args)
        mMediaPlayer = MediaPlayer(mLibVLC)
        mVideoLayout = findViewById(R.id.video_layout)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer?.release()
        mLibVLC!!.release()
    }

    override fun onStart() {
        super.onStart()
//        mMediaPlayer?.attachViews(mVideoLayout!!, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW)
//        try {
//            val media = Media(mLibVLC, assets.openFd(ASSET_FILENAME))
//            mMediaPlayer?.setMedia(media)
//            media.release()
//        } catch (e: IOException) {
//            throw RuntimeException("Invalid asset folder")
//        }
//        mMediaPlayer?.play()
    }

    override fun onStop() {
        super.onStop()
        mMediaPlayer?.stop()
        mMediaPlayer?.detachViews()
    }

}