package pt.kitsupixel.kpanime.ui.shows

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.GridLayoutManager
import pt.kitsupixel.kpanime.BuildConfig
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.adapters.ShowItemAdapter
import pt.kitsupixel.kpanime.adapters.ShowItemClickListener
import pt.kitsupixel.kpanime.databinding.ShowsFragmentBinding
import pt.kitsupixel.kpanime.domain.Show
import pt.kitsupixel.kpanime.ui.detail.DetailActivity
import pt.kitsupixel.kpanime.ui.main.MainActivity
import timber.log.Timber
import java.util.*


class ShowsFragment : Fragment() {

    companion object {
        fun newInstance() = ShowsFragment()
    }

    private val viewModel: ShowsViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(this, ShowsViewModel.Factory(activity.application))
            .get(ShowsViewModel::class.java)
    }

    private lateinit var binding: ShowsFragmentBinding

    private lateinit var viewModelAdapter: ShowItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.shows_fragment,
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val myActionMenuItem: MenuItem? = menu.findItem(R.id.app_bar_search)
        myActionMenuItem?.isVisible = true
        val searchView = myActionMenuItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterResults(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterResults(newText)
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController()
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    private fun setupViews() {
        // Click Listener for Recycler View
        viewModelAdapter = ShowItemAdapter(ShowItemClickListener { showId ->
            startActivity(
                Intent(context, DetailActivity::class.java)
                    .putExtra("showId", showId)
            )
        })

        // Initialize Recycler View
        binding.showsRecyclerView.apply {
            setHasFixedSize(true)
            adapter = viewModelAdapter
        }

        setAdapterToShows()

        // Set action of swipe to refresh
        binding.showsSwipeRefresh.setOnRefreshListener {
            if (BuildConfig.Logging) Timber.i("onRefresh called from SwipeRefreshLayout")
            viewModel.refresh()
        }
    }

    private fun setAdapterToShows() {
        viewModel.shows.observe(viewLifecycleOwner, Observer { shows ->
            shows?.apply {
                viewModelAdapter.submitList(shows)
            }
        })
    }

    fun filterResults(query: String?) {
        if (query != null || query != "") {
            viewModel.shows.removeObservers(viewLifecycleOwner)

            val queryLower = query?.toLowerCase(Locale.getDefault()).toString()
            val filteredList: MutableList<Show> = mutableListOf()
            val currentShows = viewModel.shows.value
            if (currentShows != null) {
                for (show in currentShows) {
                    if (show.title.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                        filteredList.add(show)
                    }
                }
            }

            viewModelAdapter.submitList(filteredList)
        } else {
            setAdapterToShows()
        }
    }

}

