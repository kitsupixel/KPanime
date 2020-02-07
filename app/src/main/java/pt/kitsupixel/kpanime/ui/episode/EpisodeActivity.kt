package pt.kitsupixel.kpanime.ui.episode

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.github.se_bastiaan.torrentstream.StreamStatus
import com.github.se_bastiaan.torrentstream.Torrent
import com.github.se_bastiaan.torrentstream.TorrentOptions
import com.github.se_bastiaan.torrentstream.TorrentStream
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.LinkItemAdapter
import pt.kitsupixel.kpanime.adapters.LinkItemClickListener
import pt.kitsupixel.kpanime.databinding.ActivityEpisodeBinding
import pt.kitsupixel.kpanime.domain.Link
import timber.log.Timber


class EpisodeActivity : AppCompatActivity(), TorrentListener {

    private val viewModel: EpisodeViewModel by lazy {
        ViewModelProvider(this, EpisodeViewModel.Factory(this.application, showId, episodeId))
            .get(
                EpisodeViewModel::class.java
            )
    }

    private lateinit var binding: ActivityEpisodeBinding

    private lateinit var viewModelAdapter: LinkItemAdapter

    private lateinit var torrentStream: TorrentStream

    private var showId: Long = 0L
    private var episodeId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create the slide in animation
        overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_episode)

        showId = intent.getLongExtra("showId", 0L)
        episodeId = intent.getLongExtra("episodeId", 0L)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        setClickListeners()

        setRecyclerView()

        setSwipeRefresh()

        if (!BuildConfig.noAds) {
            setInterstitialAd()
        }

        val torrentOptions: TorrentOptions = TorrentOptions.Builder()
            .saveLocation(applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS))
            .removeFilesAfterStop(true)
            .build()

        torrentStream = TorrentStream.init(torrentOptions)
        torrentStream.addListener(this)
    }

    private fun setRecyclerView() {
        binding.episodeRecyclerView.apply {
            setHasFixedSize(true)

            layoutManager =
                if (this.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT)
                    GridLayoutManager(context, 2)
                else
                    GridLayoutManager(context, 4)
            adapter = viewModelAdapter
        }

        viewModel.links.observe(this, Observer { links ->
            links?.apply {
                viewModelAdapter.submitList(links)
            }
        })
    }

    override fun finish() {
        super.finish()
        // Slide out animation
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }

    private fun setClickListeners() {
        viewModelAdapter =
            LinkItemAdapter(LinkItemClickListener { link ->
                linkClicked(link)
            })
    }


    private fun linkClicked(link: Link) {
        try {
            if (viewModel.episode.value?.type == "episode" && (link.type == "Torrent" || link.type == "Magnet")) {
                torrentStream.startStream(link.link)
                binding.progressBarHolder.visibility = View.VISIBLE
            } else {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.data = Uri.parse(link.link)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Service unavailable", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onStreamReady(torrent: Torrent?) {
        if (BuildConfig.Logging) Timber.i("onStreamReady")
        binding.progressBar.progress = 100
        binding.progressBarHolder.visibility = View.GONE

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(torrent?.videoFile.toString()))
        intent.setDataAndType(Uri.parse(torrent?.videoFile.toString()), "video/mp4")
        startActivity(intent)
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
        if (status != null && binding.progressBar.progress != status.bufferProgress) {
            if (BuildConfig.Logging) Timber.i("Progress: " + status?.bufferProgress)
            binding.progressBar.isIndeterminate = false
            binding.progressBar.progress = status.bufferProgress
        }
    }

    override fun onStreamError(torrent: Torrent?, e: java.lang.Exception?) {
        if (BuildConfig.Logging) Timber.i("onStreamError")
    }

    private fun setSwipeRefresh() {
        binding.episodeSwipeRefresh.setOnRefreshListener {
            if (BuildConfig.Logging) Timber.i("onRefresh called from SwipeRefreshLayout")
            viewModel.refresh()
        }

        viewModel.refreshing.observe(this, Observer { refreshing ->
            refreshing?.apply {
                binding.episodeSwipeRefresh.isRefreshing = refreshing
            }
        })
    }

    private fun setInterstitialAd() {
        val mInterstitialAd = InterstitialAd(this)

        if (BuildConfig.AdmobTest) {
            if (BuildConfig.Logging) Timber.i("Using test ad")
            mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712" // Test Ads
        } else {
            mInterstitialAd.adUnitId = "ca-app-pub-7666356884507044/7588812185" // Prod Ads
        }

        mInterstitialAd.loadAd(AdRequest.Builder().build())

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                mInterstitialAd.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BuildConfig.Logging) Timber.i("onDestroy")
        torrentStream.stopStream()
    }
}
