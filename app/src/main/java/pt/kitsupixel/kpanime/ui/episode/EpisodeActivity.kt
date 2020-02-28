package pt.kitsupixel.kpanime.ui.episode

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.se_bastiaan.torrentstream.Torrent
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.LinkItemAdapter
import pt.kitsupixel.kpanime.adapters.LinkItemClickListener
import pt.kitsupixel.kpanime.databinding.ActivityEpisodeBinding
import pt.kitsupixel.kpanime.domain.Link
import timber.log.Timber


class EpisodeActivity : AppCompatActivity() {

    private val viewModel: EpisodeViewModel by lazy {
        ViewModelProvider(this, EpisodeViewModel.Factory(this.application, showId, episodeId))
            .get(
                EpisodeViewModel::class.java
            )
    }

    private lateinit var binding: ActivityEpisodeBinding

    private lateinit var viewModelAdapter: LinkItemAdapter

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

        setupViews()

        viewModel.episode.observe(this, Observer { episode ->
            if (episode != null && episode.type == "episode") {
                this.title =
                    String.format(resources.getString(R.string.episode_text), episode.number)
            } else if (episode != null) {
                this.title =
                    String.format(resources.getString(R.string.batch_text), episode.number)
            }
        })

        setSupportActionBar(binding.episodeToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    private fun setupViews() {
        viewModelAdapter =
            LinkItemAdapter(LinkItemClickListener { link ->
                linkClicked(link)
            })

        binding.playButton.setOnClickListener {
            val selectedItem = binding.streamSpinner.selectedItem.toString()

            var urlToStream: String? = null

            when {
                selectedItem.startsWith("480p") -> urlToStream = viewModel.torrent480p.value?.link
                selectedItem.startsWith("720p") -> urlToStream = viewModel.torrent720p.value?.link
                selectedItem.startsWith("1080p") -> urlToStream = viewModel.torrent1080p.value?.link
            }

            streamEpisode(urlToStream)
        }

        binding.episodeRecyclerView.apply {
            setHasFixedSize(true)
            adapter = viewModelAdapter
        }

        viewModel.links.observe(this, Observer { links ->

            links?.apply {
                viewModelAdapter.submitList(links)

                if (viewModel.episode.value?.type == "episode") {
                    for (link in links) {
                        if (link.quality == "480p" && (link.type == "Torrent" || (link.type == "Magnet" && viewModel.torrent480p.value == null))) {
                            viewModel.setTorrent480Link(link)
                            viewModel.setTextViewable(true)

                        } else if (link.quality == "720p" && (link.type == "Torrent" || (link.type == "Magnet" && viewModel.torrent720p.value == null))) {
                            viewModel.setTorrent720Link(link)
                            viewModel.setTextViewable(true)
                        } else if (link.quality == "1080p" && (link.type == "Torrent" || (link.type == "Magnet" && viewModel.torrent1080p.value == null))) {
                            viewModel.setTorrent1080Link(link)
                            viewModel.setTextViewable(true)
                        }
                    }
                }

                val items = mutableListOf<String?>()
                if (viewModel.torrent480p.value != null) {
                    val link = viewModel.torrent480p.value
                    link?.apply {
                        items.add(
                            String.format(
                                getString(R.string.stream_spinner_quality),
                                link.quality,
                                link.seeds,
                                link.leeches
                            )
                        )
                    }

                }
                if (viewModel.torrent720p.value != null) {
                    val link = viewModel.torrent720p.value
                    link?.apply {
                        items.add(
                            String.format(
                                getString(R.string.stream_spinner_quality),
                                link.quality,
                                link.seeds,
                                link.leeches
                            )
                        )
                    }
                }
                if (viewModel.torrent1080p.value != null) {
                    val link = viewModel.torrent1080p.value
                    link?.apply {
                        items.add(
                            String.format(
                                getString(R.string.stream_spinner_quality),
                                link.quality,
                                link.seeds,
                                link.leeches
                            )
                        )
                    }
                }

                if (items.size > 0) {
                    val spinnerAdapter = ArrayAdapter(
                        baseContext,
                        android.R.layout.simple_spinner_dropdown_item,
                        items
                    )
                    binding.streamSpinner.apply {
                        adapter = spinnerAdapter
                    }
                }
            }
        })

        binding.episodeSwipeRefresh.setOnRefreshListener {
            if (BuildConfig.Logging) Timber.i("onRefresh called from SwipeRefreshLayout")
            viewModel.refresh()
        }

        viewModel.openPlayer.observe(this, Observer { isOpenPlayer ->
            if (isOpenPlayer == true) {
                val torrent: Torrent? = viewModel.torrent.value
                if (torrent != null) {
                    try {
                        val intent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(torrent.videoFile.toString()))
                        intent.setDataAndType(Uri.parse(torrent.videoFile.toString()), "video/*")
                        startActivity(intent)

                        viewModel.markEpisodeWatched()

                        viewModel.endLoading()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.service_unavailable),
                            Toast.LENGTH_SHORT
                        ).show()
                        e.printStackTrace()
                    }
                }
            }
        })

        val adView: AdView = binding.adView
        val adRequest: AdRequest = AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .build()

        adView.loadAd(adRequest)
    }

    private fun linkClicked(link: Link) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.data = Uri.parse(link.link)
            startActivity(intent)
            viewModel.markEpisodeDownloaded()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                resources.getString(R.string.service_unavailable),
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    private fun streamEpisode(url: String?) {
        if (url != null) {
            if (!viewModel.torrentStream.isStreaming) {
                viewModel.startStream(url)
            } else {
                if (viewModel.torrentStream.currentTorrentUrl == url) {
                    viewModel.forceOpenPlayer()
                } else {
                    viewModel.torrentStream.stopStream()
                    viewModel.startStream(url)
                }
            }
        } else {
            Toast.makeText(
                this,
                "There is no valid torrent to stream",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }

    override fun finish() {
        super.finish()
        // Slide out animation
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }

}
