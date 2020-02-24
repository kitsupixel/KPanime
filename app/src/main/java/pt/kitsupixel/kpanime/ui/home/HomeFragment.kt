package pt.kitsupixel.kpanime.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.HomeShowItemAdapter
import pt.kitsupixel.kpanime.adapters.HomeShowItemClickListener
import pt.kitsupixel.kpanime.databinding.HomeFragmentBinding
import pt.kitsupixel.kpanime.domain.Show
import pt.kitsupixel.kpanime.ui.detail.DetailActivity
import timber.log.Timber


class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(this, HomeViewModel.Factory(activity.application))
            .get(HomeViewModel::class.java)
    }

    private lateinit var binding: HomeFragmentBinding

    private lateinit var viewModelAdapterFavorites: HomeShowItemAdapter
    private lateinit var viewModelAdapterCurrent: HomeShowItemAdapter
    private lateinit var viewModelAdapterLatest: HomeShowItemAdapter
    private lateinit var viewModelAdapterWatched: HomeShowItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.home_fragment,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        setupViews()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController()
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    private fun setupViews() {
        // Click Listener for Recycler View
        viewModelAdapterLatest = HomeShowItemAdapter(HomeShowItemClickListener { showId ->
            startActivity(
                Intent(context, DetailActivity::class.java)
                    .putExtra("showId", showId)
            )
        })

        viewModelAdapterFavorites = HomeShowItemAdapter(HomeShowItemClickListener { showId ->
            startActivity(
                Intent(context, DetailActivity::class.java)
                    .putExtra("showId", showId)
            )
        })

        viewModelAdapterCurrent = HomeShowItemAdapter(HomeShowItemClickListener { showId ->
            startActivity(
                Intent(context, DetailActivity::class.java)
                    .putExtra("showId", showId)
            )
        })

        viewModelAdapterWatched = HomeShowItemAdapter(HomeShowItemClickListener { showId ->
            startActivity(
                Intent(context, DetailActivity::class.java)
                    .putExtra("showId", showId)
            )
        })


        // Initialize Recycler View
        binding.homeLatestRecyclerview.apply {
//            setHasFixedSize(true)
            adapter = viewModelAdapterLatest
        }

        binding.homeFavoritesRecyclerview.apply {
//            setHasFixedSize(true)
            adapter = viewModelAdapterFavorites
        }

        binding.homeCurrentRecyclerview.apply {
//            setHasFixedSize(true)
            adapter = viewModelAdapterCurrent
        }

        binding.homeWatchedRecyclerview.apply {
//            setHasFixedSize(true)
            adapter = viewModelAdapterWatched
        }

        setAdapterToShows()

        // Set action of swipe to refresh
        binding.homeSwipeRefresh.setOnRefreshListener {
            if (BuildConfig.Logging) Timber.i("onRefresh called from SwipeRefreshLayout")
            viewModel.refresh()
        }
    }

    private fun setAdapterToShows() {
        viewModel.latest.observe(viewLifecycleOwner, Observer { episodeAndShows ->
            if (episodeAndShows != null) {
                // Convert EpisodeAndShow to a list of shows
                val shows: MutableList<Show> = mutableListOf()
                for (item in episodeAndShows) {
                    shows.add(item.show)
                }

                shows.apply {
                    viewModelAdapterLatest.submitList(shows)
                    viewModelAdapterLatest.notifyDataSetChanged()
                }
            }
        })

        viewModel.favorites.observe(viewLifecycleOwner, Observer { shows ->
            shows?.apply {
                viewModelAdapterFavorites.submitList(shows)
                viewModelAdapterFavorites.notifyDataSetChanged()
            }
        })

        viewModel.current.observe(viewLifecycleOwner, Observer { shows ->
            shows?.apply {
                viewModelAdapterCurrent.submitList(shows)
                viewModelAdapterCurrent.notifyDataSetChanged()
            }
        })

        viewModel.watched.observe(viewLifecycleOwner, Observer { shows ->
            shows?.apply {
                viewModelAdapterWatched.submitList(shows)
                viewModelAdapterWatched.notifyDataSetChanged()
            }
        })
    }
}
