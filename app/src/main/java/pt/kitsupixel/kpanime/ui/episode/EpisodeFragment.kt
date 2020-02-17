package pt.kitsupixel.kpanime.ui.episode

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.se_bastiaan.torrentstream.Torrent
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.LinkItemAdapter
import pt.kitsupixel.kpanime.adapters.LinkItemClickListener
import pt.kitsupixel.kpanime.databinding.EpisodeFragmentBinding
import pt.kitsupixel.kpanime.domain.Link
import pt.kitsupixel.kpanime.ui.main.MainActivity
import timber.log.Timber


class EpisodeFragment : Fragment() {

    private val viewModel: EpisodeViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(this, EpisodeViewModel.Factory(activity.application, showId, episodeId))
            .get(
                EpisodeViewModel::class.java
            )
    }

    private val args: EpisodeFragmentArgs by navArgs()

    private lateinit var binding: EpisodeFragmentBinding

    private lateinit var viewModelAdapter: LinkItemAdapter

    private var showId: Long = 0L

    private var episodeId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.episode_fragment,
            container,
            false
        )

        showId = args.showId
        episodeId = args.episodeId

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        setClickListeners()

        setRecyclerView()

        setSwipeRefresh()

        setTorrentStreamListener()

        viewModel.episode.observe(viewLifecycleOwner, Observer { episode ->
            if (episode != null)
                this.activity?.title = episode.type.capitalize() + " " + episode.number
        })

        return binding.root
    }

    private fun setTorrentStreamListener() {
        viewModel.openPlayer.observe(viewLifecycleOwner, Observer { isOpenPlayer ->
            if (isOpenPlayer == true) {
                val torrent: Torrent? = viewModel.torrent.value
                if (torrent != null) {
                    try {
                        val intent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(torrent.videoFile.toString()))
                        intent.setDataAndType(Uri.parse(torrent.videoFile.toString()), "video/mp4")
                        startActivity(intent)

                        viewModel.endLoading()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Service unavailable", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun setRecyclerView() {
        binding.episodeRecyclerView.apply {
            setHasFixedSize(true)
            adapter = viewModelAdapter
        }

        viewModel.links.observe(viewLifecycleOwner, Observer { links ->

            links?.apply {
                viewModelAdapter.submitList(links)

                if (viewModel.episode.value?.type == "episode") {
                    for (link in links) {
                        if (link.quality == "480p" && (link.type == "Torrent" || (link.type == "Magnet" && viewModel.torrent480p.value == null))) {
                            viewModel.setTorrent480Link(link.link)
                            viewModel.setTextViewable(true)
                        } else if (link.quality == "720p" && (link.type == "Torrent" || (link.type == "Magnet" && viewModel.torrent720p.value == null))) {
                            viewModel.setTorrent720Link(link.link)
                            viewModel.setTextViewable(true)
                        } else if (link.quality == "1080p" && (link.type == "Torrent" || (link.type == "Magnet" && viewModel.torrent1080p.value == null))) {
                            viewModel.setTorrent1080Link(link.link)
                            viewModel.setTextViewable(true)
                        }
                    }
                }
            }
        })
    }

    private fun setClickListeners() {
        viewModelAdapter =
            LinkItemAdapter(LinkItemClickListener { link ->
                linkClicked(link)
            })

        binding.button480.setOnClickListener {
            streamEpisode(viewModel.torrent480p.value)
        }

        binding.button720.setOnClickListener {
            streamEpisode(viewModel.torrent720p.value)
        }

        binding.button1080.setOnClickListener {
            streamEpisode(viewModel.torrent1080p.value)
        }
    }

    private fun linkClicked(link: Link) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.data = Uri.parse(link.link)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Service unavailable", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


    private fun streamEpisode(url: String?) {
        if (url != null) viewModel.startStream(url)
    }

    private fun setSwipeRefresh() {
        binding.episodeSwipeRefresh.setOnRefreshListener {
            if (BuildConfig.Logging) Timber.i("onRefresh called from SwipeRefreshLayout")
            viewModel.refresh()
        }
    }
}
