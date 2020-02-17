package pt.kitsupixel.kpanime.ui.detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.EpisodeItemAdapter
import pt.kitsupixel.kpanime.adapters.EpisodeItemClickListener
import pt.kitsupixel.kpanime.databinding.DetailFragmentBinding
import pt.kitsupixel.kpanime.ui.main.MainActivity
import timber.log.Timber

class DetailFragment : Fragment() {

    companion object {
        fun newInstance() = DetailFragment()
    }

    private val viewModel: DetailViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(this, DetailViewModel.Factory(activity.application, showId)).get(
            DetailViewModel::class.java
        )
    }


    private val args: DetailFragmentArgs by navArgs()

    private lateinit var binding: DetailFragmentBinding

    private lateinit var viewModelAdapter: EpisodeItemAdapter

    private var showId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.detail_fragment,
            container,
            false
        )

        showId = args.showId

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setClickListener()

        setRecyclerView()

        return binding.root
    }

    private fun setRecyclerView() {
        binding.detailEpisodesRecyclerview.apply {
            //setHasFixedSize(true)
            adapter = viewModelAdapter
        }

        viewModel.episodes.observe(viewLifecycleOwner, Observer { episodes ->
            episodes?.apply {
                viewModelAdapter.submitList(episodes)
            }
        })
    }

    private fun setClickListener() {
        viewModelAdapter =
            EpisodeItemAdapter(EpisodeItemClickListener { episodeId ->
                Navigation.findNavController(this.view!!)
                    .navigate(
                        DetailFragmentDirections.actionDetailFragmentToEpisodeFragment()
                            .setShowId(showId)
                            .setEpisodeId(episodeId)
                    )
            })

        viewModel.show.observe(viewLifecycleOwner, Observer { show ->
            when (show?.favorite) {
                true -> binding.favouriteFab.setImageResource(R.drawable.ic_favourite)
                else -> binding.favouriteFab.setImageResource(R.drawable.ic_unfavourite)
            }
            // Workaround for bug on lib 28.0.0
            binding.favouriteFab.hide()
            binding.favouriteFab.show()
        })

        viewModel.eventFavorite.observe(viewLifecycleOwner, Observer { isFavourite ->
            if (isFavourite != null) {
                if (isFavourite == true)
                    Snackbar.make(
                        this.activity?.findViewById(android.R.id.content)!!,
                        "This show was added to your favourites",
                        Snackbar.LENGTH_SHORT
                    ).show()
                else if (isFavourite == false)
                    Snackbar.make(
                        this.activity?.findViewById(android.R.id.content)!!,
                        "This show was removed from your favourites",
                        Snackbar.LENGTH_SHORT
                    ).show()

                if (BuildConfig.Logging) Timber.i("favourite is $isFavourite")

                viewModel.eventFavoriteClear()
            }
        })
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onDetach() {
        (activity as MainActivity).showBottomNavigation()
        super.onDetach()
    }
}
