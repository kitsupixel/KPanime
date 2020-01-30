package pt.kitsupixel.kpanime.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
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
        ViewModelProviders.of(this, DetailViewModel.Factory(activity.application, showId))
            .get(DetailViewModel::class.java)
    }

    private lateinit var binding: DetailFragmentBinding

    private lateinit var viewModelAdapter: EpisodeItemAdapter

    private var showId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.detail_fragment,
            container,
            false
        )
        showId = arguments?.getLong("showId")!!

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel


        viewModelAdapter =
            EpisodeItemAdapter(EpisodeItemClickListener { episodeId ->
                episodeClicked(episodeId)
            })

        binding.showEpisodesRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viewModelAdapter
        }

        viewModel.eventFavorite.observe(viewLifecycleOwner, Observer { isFavourite ->
            if (isFavourite != null) {
                if (isFavourite == true)
                    Snackbar.make(
                        activity?.findViewById(android.R.id.content)!!,
                        "This show was added to your favourites",
                        Snackbar.LENGTH_SHORT
                    ).show()
                else if (isFavourite == false)
                    Snackbar.make(
                        activity?.findViewById(android.R.id.content)!!,
                        "This show was removed from your favourites",
                        Snackbar.LENGTH_SHORT
                    ).show()

                Timber.i("favourite is $isFavourite")

                viewModel.eventFavoriteClear()
            }
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

        (activity as MainActivity?)?.hideActionBar()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.episodes.observe(viewLifecycleOwner, Observer { episodes ->
            episodes?.apply {
                viewModelAdapter.submitList(episodes)
            }
        })
    }

    override fun onDetach() {
        super.onDetach()
        (activity as MainActivity?)?.showActionBar()
    }

    private fun episodeClicked(episodeId: Long) {
        val directions = DetailFragmentDirections.actionDetailFragmentToEpisodeFragment()
            .setEpisodeId(episodeId)
            .setShowId(showId)
        Navigation.findNavController(this.view!!).navigate(directions)
    }

}
