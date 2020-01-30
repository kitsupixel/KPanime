package pt.kitsupixel.kpanime.ui.episode

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
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.LinkItemAdapter
import pt.kitsupixel.kpanime.adapters.LinkItemClickListener
import pt.kitsupixel.kpanime.databinding.EpisodeFragmentBinding
import pt.kitsupixel.kpanime.domain.Link
import pt.kitsupixel.kpanime.ui.main.MainActivity
import timber.log.Timber


class EpisodeFragment : Fragment() {

    companion object {
        fun newInstance() = EpisodeFragment()
    }

    private val viewModel: EpisodeViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, EpisodeViewModel.Factory(activity.application, showId, episodeId))
            .get(EpisodeViewModel::class.java)
    }

    private lateinit var binding: EpisodeFragmentBinding

    private lateinit var viewModelAdapter: LinkItemAdapter

    private var showId: Long = 0L
    private var episodeId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.episode_fragment,
            container,
            false
        )
        showId = arguments?.getLong("showId")!!
        episodeId = arguments?.getLong("episodeId")!!

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModelAdapter =
            LinkItemAdapter(LinkItemClickListener { link ->
                linkClicked(link)
            })

        binding.episodeRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viewModelAdapter
        }

        //(activity as MainActivity?)?.hideActionBar()

        return binding.root
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.episode.observe(viewLifecycleOwner, Observer { episode ->
            Timber.i("Number of links: " + episode?.links?.size.toString())
            episode?.links.apply {
                viewModelAdapter.submitList(this)
            }
        })
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

}
